package com.example.solver;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import com.example.domain.Connection;
import com.example.domain.ScheduledTrip;
import com.example.domain.Station;
import com.example.domain.TrackOccupancy;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/**
 * {@link ConstraintProvider} for the Train Schedule Optimization problem.
 */
public class TrainScheduleConstraintProvider implements ConstraintProvider {

    private static final int MINIMUM_HEADWAY_SECONDS = 180; // 3 minutes minimum between trains

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                trainMustBeAssigned(constraintFactory),
                departureTimeMustBeAssigned(constraintFactory),
                trainCannotOverlap(constraintFactory),
                singleTrackConflict(constraintFactory),
                minimumHeadway(constraintFactory),
                trainMustStartAtDepot(constraintFactory),
                trainMustEndAtDepot(constraintFactory),

                // Soft constraints
                preferEvenHeadways(constraintFactory),
                minimizeTrainIdleTime(constraintFactory),
                balanceTrainUtilization(constraintFactory),
                // preferRoundDepartureTimes(constraintFactory)
        };
    }

    // ========== HARD CONSTRAINTS ==========

    /**
     * Every scheduled trip must have a train assigned.
     */
    protected Constraint trainMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getAssignedTrain() == null)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Train must be assigned");
    }

    /**
     * Every scheduled trip must have a departure time assigned.
     */
    protected Constraint departureTimeMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getDepartureTime() == null)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Departure time must be assigned");
    }

    /**
     * A train cannot be assigned to overlapping trips.
     * Ensures a train finishes one trip before starting another.
     * Uses pre-computed route travel time for faster estimation.
     */
    protected Constraint trainCannotOverlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getAssignedTrain))
                .filter((t1, t2) -> t1.getAssignedTrain() != null
                        && t1.getDepartureTime() != null
                        && t2.getDepartureTime() != null)
                .filter((t1, t2) -> {
                    // Use fast trip duration calculation
                    LocalDateTime arrival1 = estimateArrivalTime(t1);
                    LocalDateTime arrival2 = estimateArrivalTime(t2);
                    if (arrival1 == null || arrival2 == null) {
                        return false;
                    }
                    // Check if time intervals overlap
                    return t1.getDepartureTime().isBefore(arrival2)
                            && t2.getDepartureTime().isBefore(arrival1);
                })
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Train cannot have overlapping trips");
    }

    /**
     * Trains cannot occupy single-track segments in opposite directions simultaneously.
     * This prevents head-on collisions.
     *
     * Optimization: Only check trips that share at least one connection.
     */
    protected Constraint singleTrackConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class)
                .filter((t1, t2) -> t1.getDepartureTime() != null && t1.getAssignedTrain() != null
                        && t2.getDepartureTime() != null && t2.getAssignedTrain() != null)
                // Quick check: could these trips possibly overlap in time?
                .filter((t1, t2) -> couldOverlapInTime(t1, t2))
                // Quick check: do these trips share any connections?
                .filter((t1, t2) -> shareAnyConnection(t1, t2))
                // Detailed check only if quick checks pass
                .filter((t1, t2) -> hasTrackConflict(t1, t2))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Single track conflict");
    }

    /**
     * Minimum headway between trains on the same track segment in the same direction.
     * Prevents following too closely.
     *
     * Optimization: Only check trips on the same route.
     */
    protected Constraint minimumHeadway(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class,
                        // Only check trips on same route - they share track segments
                        Joiners.equal(ScheduledTrip::getRoute))
                .filter((t1, t2) -> t1.getDepartureTime() != null && t1.getAssignedTrain() != null
                        && t2.getDepartureTime() != null && t2.getAssignedTrain() != null)
                // Quick check: are departures close enough that headway could be violated?
                .filter((t1, t2) -> {
                    long gapSeconds = Math.abs(ChronoUnit.SECONDS.between(
                            t1.getDepartureTime(), t2.getDepartureTime()));
                    // If departures are far apart, no headway violation possible
                    int maxTripDuration = Math.max(
                            getTripDurationSeconds(t1),
                            getTripDurationSeconds(t2));
                    return gapSeconds < maxTripDuration + MINIMUM_HEADWAY_SECONDS;
                })
                .filter((t1, t2) -> hasHeadwayViolation(t1, t2))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Minimum headway violation");
    }

    /**
     * The first trip of a train must start at its depot.
     */
    protected Constraint trainMustStartAtDepot(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getAssignedTrain() != null
                        && trip.getDepartureTime() != null
                        && trip.getRoute() != null)
                // Find the first trip for this train (no earlier trip exists)
                .ifNotExists(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getAssignedTrain),
                        Joiners.lessThan(ScheduledTrip::getDepartureTime))
                .filter(trip -> {
                    Station firstStation = trip.getRoute().getFirstStop(trip.getDirection());
                    Station depot = trip.getAssignedTrain().getDepot();
                    return depot != null && firstStation != null && !firstStation.equals(depot);
                })
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Train must start at depot");
    }

    /**
     * The last trip of a train must end at its depot.
     */
    protected Constraint trainMustEndAtDepot(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getAssignedTrain() != null
                        && trip.getDepartureTime() != null
                        && trip.getRoute() != null)
                // Find the last trip for this train (no later trip exists)
                .ifNotExists(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getAssignedTrain),
                        Joiners.greaterThan(ScheduledTrip::getDepartureTime))
                .filter(trip -> {
                    Station lastStation = trip.getRoute().getLastStop(trip.getDirection());
                    Station depot = trip.getAssignedTrain().getDepot();
                    return depot != null && lastStation != null && !lastStation.equals(depot);
                })
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Train must end at depot");
    }

    // ========== SOFT CONSTRAINTS ==========

    /**
     * Prefer even headways between CONSECUTIVE trips on the same route/direction.
     * Only penalizes the gap to the next trip, not all pairs.
     */
    protected Constraint preferEvenHeadways(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getDepartureTime() != null && trip.getAssignedTrain() != null)
                .join(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getRoute),
                        Joiners.equal(ScheduledTrip::getDirection),
                        Joiners.lessThan(ScheduledTrip::getDepartureTime))
                .filter((t1, t2) -> t2.getDepartureTime() != null && t2.getAssignedTrain() != null)
                // Only consider consecutive trips (no trip in between)
                .ifNotExists(ScheduledTrip.class,
                        Joiners.equal((t1, t2) -> t1.getRoute(), ScheduledTrip::getRoute),
                        Joiners.equal((t1, t2) -> t1.getDirection(), ScheduledTrip::getDirection),
                        Joiners.greaterThan((t1, t2) -> t1.getDepartureTime(), ScheduledTrip::getDepartureTime),
                        Joiners.lessThan((t1, t2) -> t2.getDepartureTime(), ScheduledTrip::getDepartureTime))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (t1, t2) -> {
                            long gapMinutes = ChronoUnit.MINUTES.between(t1.getDepartureTime(), t2.getDepartureTime());
                            long idealHeadwayMinutes = 120; // 2 hours based on dataset interval
                            long deviation = Math.abs(gapMinutes - idealHeadwayMinutes);
                            // Only penalize significant deviations (> 15 min)
                            return deviation > 15 ? (int) (deviation - 15) : 0;
                        })
                .asConstraint("Prefer even headways");
    }

    /**
     * Minimize idle time between consecutive trips for the same train.
     * Encourages efficient train utilization.
     */
    protected Constraint minimizeTrainIdleTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getDepartureTime() != null && trip.getAssignedTrain() != null)
                .join(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getAssignedTrain),
                        Joiners.lessThan(ScheduledTrip::getDepartureTime))
                .filter((t1, t2) -> t2.getDepartureTime() != null)
                // Only consider consecutive trips for same train
                .ifNotExists(ScheduledTrip.class,
                        Joiners.equal((t1, t2) -> t1.getAssignedTrain(), ScheduledTrip::getAssignedTrain),
                        Joiners.greaterThan((t1, t2) -> t1.getDepartureTime(), ScheduledTrip::getDepartureTime),
                        Joiners.lessThan((t1, t2) -> t2.getDepartureTime(), ScheduledTrip::getDepartureTime))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (t1, t2) -> {
                            LocalDateTime arrival1 = getArrivalTime(t1);
                            if (arrival1 == null) return 0;
                            long idleMinutes = ChronoUnit.MINUTES.between(arrival1, t2.getDepartureTime());
                            // Allow some buffer (turnaround + positioning)
                            long acceptableIdle = 30; // 30 minutes acceptable
                            return idleMinutes > acceptableIdle ? (int) (idleMinutes - acceptableIdle) : 0;
                        })
                .asConstraint("Minimize train idle time");
    }

    /**
     * Prefer balanced train utilization - penalize trains with too few trips.
     */
    protected Constraint balanceTrainUtilization(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getAssignedTrain() != null)
                .groupBy(ScheduledTrip::getAssignedTrain, ConstraintCollectors.count())
                .filter((train, count) -> count < 5) // Penalize underutilized trains
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (train, count) -> (5 - count) * 10) // Weight: 10 per missing trip
                .asConstraint("Balance train utilization");
    }

    /**
     * Prefer trips to depart on round times (e.g., :00, :15, :30, :45).
     * Makes schedules easier for passengers to remember.
     * Lower weight - this is a nice-to-have, not critical.
     */
    protected Constraint preferRoundDepartureTimes(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduledTrip.class)
                .filter(trip -> trip.getDepartureTime() != null)
                .filter(trip -> {
                    int minute = trip.getDepartureTime().getMinute();
                    // Only penalize if more than 5 minutes from quarter hour
                    int distTo00 = minute;
                    int distTo15 = Math.abs(minute - 15);
                    int distTo30 = Math.abs(minute - 30);
                    int distTo45 = Math.abs(minute - 45);
                    int distTo60 = 60 - minute;
                    int minDist = Math.min(Math.min(distTo00, distTo15),
                                          Math.min(Math.min(distTo30, distTo45), distTo60));
                    return minDist > 5; // Only penalize significant deviations
                })
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint("Prefer round departure times");
    }

    // ========== HELPER METHODS ==========

    /**
     * Get arrival time from shadow variable (cached) or compute if not available.
     */
    private LocalDateTime getArrivalTime(ScheduledTrip trip) {
        // Use cached shadow variable if available
        if (trip.getArrivalTime() != null) {
            return trip.getArrivalTime();
        }
        // Fall back to computation if shadow variable not yet set
        return estimateArrivalTime(trip);
    }

    /**
     * Fast estimation of arrival time using pre-computed route travel time.
     */
    private LocalDateTime estimateArrivalTime(ScheduledTrip trip) {
        if (trip.getDepartureTime() == null || trip.getAssignedTrain() == null || trip.getRoute() == null) {
            return null;
        }
        int durationSeconds = getTripDurationSeconds(trip);
        return trip.getDepartureTime().plusSeconds(durationSeconds);
    }

    /**
     * Get total trip duration in seconds using pre-computed route data.
     */
    private int getTripDurationSeconds(ScheduledTrip trip) {
        if (trip.getRoute() == null || trip.getAssignedTrain() == null) {
            return 0;
        }
        // Base travel time from pre-computed route data
        int baseTravelTime = trip.getRoute().getBaseTravelTimeSeconds(trip.getDirection());
        int adjustedTravelTime = trip.getAssignedTrain().getAdjustedTravelTime(baseTravelTime);

        // Add dwell times at intermediate stations
        List<Station> stops = trip.getRoute().getStopsInDirection(trip.getDirection());
        int totalDwellTime = 0;
        for (int i = 1; i < stops.size() - 1; i++) {
            totalDwellTime += stops.get(i).getStopDurationInSeconds();
        }

        return adjustedTravelTime + totalDwellTime;
    }

    /**
     * Quick check if two trips could possibly overlap in time.
     */
    private boolean couldOverlapInTime(ScheduledTrip t1, ScheduledTrip t2) {
        LocalDateTime arrival1 = getArrivalTime(t1);
        LocalDateTime arrival2 = getArrivalTime(t2);
        if (arrival1 == null || arrival2 == null) {
            return false;
        }
        // Time intervals overlap if start1 < end2 AND start2 < end1
        return t1.getDepartureTime().isBefore(arrival2) && t2.getDepartureTime().isBefore(arrival1);
    }

    /**
     * Quick check if two trips share any track connection.
     */
    private boolean shareAnyConnection(ScheduledTrip t1, ScheduledTrip t2) {
        List<Connection> conn1 = t1.getRoute().getRequiredConnections(t1.getDirection());
        List<Connection> conn2 = t2.getRoute().getRequiredConnections(t2.getDirection());

        // Use smaller set for lookup
        if (conn1.size() > conn2.size()) {
            List<Connection> temp = conn1;
            conn1 = conn2;
            conn2 = temp;
        }

        Set<Connection> set1 = Set.copyOf(conn1);
        for (Connection c : conn2) {
            if (set1.contains(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get track occupancies from shadow variable (cached) or compute if not available.
     */
    private List<TrackOccupancy> getTrackOccupancies(ScheduledTrip trip) {
        List<TrackOccupancy> cached = trip.getTrackOccupancies();
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        return trip.computeTrackOccupancies();
    }

    /**
     * Detailed check for track conflict (head-on collision on single track).
     * Uses cached track occupancies from shadow variable.
     */
    private boolean hasTrackConflict(ScheduledTrip t1, ScheduledTrip t2) {
        List<TrackOccupancy> occ1 = getTrackOccupancies(t1);
        List<TrackOccupancy> occ2 = getTrackOccupancies(t2);
        return occ1.stream().anyMatch(o1 ->
                occ2.stream().anyMatch(o2 -> o1.conflictsWith(o2)));
    }

    /**
     * Detailed check for headway violation.
     * Uses cached track occupancies from shadow variable.
     */
    private boolean hasHeadwayViolation(ScheduledTrip t1, ScheduledTrip t2) {
        List<TrackOccupancy> occ1 = getTrackOccupancies(t1);
        List<TrackOccupancy> occ2 = getTrackOccupancies(t2);
        return occ1.stream().anyMatch(o1 ->
                occ2.stream().anyMatch(o2 ->
                        o1.getConnection() != null
                                && o1.getConnection().equals(o2.getConnection())
                                && o1.getDirection() == o2.getDirection()
                                && o1.getEntryTime() != null && o2.getEntryTime() != null
                                && Math.abs(ChronoUnit.SECONDS.between(
                                o1.getEntryTime(), o2.getEntryTime())) < MINIMUM_HEADWAY_SECONDS
                ));
    }
}
