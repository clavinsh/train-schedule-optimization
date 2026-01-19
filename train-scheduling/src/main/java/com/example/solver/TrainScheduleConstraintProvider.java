package com.example.solver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.domain.ScheduledTrip;
import com.example.domain.Station;
import com.example.domain.TrackOccupancy;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
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
                preferEvenHeadways(constraintFactory)
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
     */
    protected Constraint trainCannotOverlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getAssignedTrain))
                .filter((t1, t2) -> t1.getAssignedTrain() != null
                        && t1.getDepartureTime() != null
                        && t2.getDepartureTime() != null)
                .filter((t1, t2) -> {
                    // Compute arrival times on demand
                    LocalDateTime arrival1 = computeArrivalTime(t1);
                    LocalDateTime arrival2 = computeArrivalTime(t2);
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
     */
    protected Constraint singleTrackConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class)
                .filter((t1, t2) -> t1.getDepartureTime() != null && t1.getAssignedTrain() != null
                        && t2.getDepartureTime() != null && t2.getAssignedTrain() != null)
                .filter((t1, t2) -> {
                    List<TrackOccupancy> occ1 = t1.computeTrackOccupancies();
                    List<TrackOccupancy> occ2 = t2.computeTrackOccupancies();
                    // Check for any conflicting occupancies
                    return occ1.stream().anyMatch(o1 ->
                            occ2.stream().anyMatch(o2 -> o1.conflictsWith(o2)));
                })
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Single track conflict");
    }

    /**
     * Minimum headway between trains on the same track segment in the same direction.
     * Prevents following too closely.
     */
    protected Constraint minimumHeadway(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class)
                .filter((t1, t2) -> t1.getDepartureTime() != null && t1.getAssignedTrain() != null
                        && t2.getDepartureTime() != null && t2.getAssignedTrain() != null)
                .filter((t1, t2) -> {
                    List<TrackOccupancy> occ1 = t1.computeTrackOccupancies();
                    List<TrackOccupancy> occ2 = t2.computeTrackOccupancies();
                    // Check for headway violations on same connection, same direction
                    return occ1.stream().anyMatch(o1 ->
                            occ2.stream().anyMatch(o2 ->
                                    o1.getConnection() != null
                                            && o1.getConnection().equals(o2.getConnection())
                                            && o1.getDirection() == o2.getDirection()
                                            && o1.getEntryTime() != null && o2.getEntryTime() != null
                                            && Math.abs(ChronoUnit.SECONDS.between(
                                            o1.getEntryTime(), o2.getEntryTime())) < MINIMUM_HEADWAY_SECONDS
                            ));
                })
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
     * Prefer even headways between trips on the same route.
     * Provides more regular service for passengers.
     */
    protected Constraint preferEvenHeadways(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(ScheduledTrip.class,
                        Joiners.equal(ScheduledTrip::getRoute),
                        Joiners.equal(ScheduledTrip::getDirection))
                .filter((t1, t2) -> t1.getDepartureTime() != null && t2.getDepartureTime() != null)
                .filter((t1, t2) -> {
                    Duration gap = Duration.between(t1.getDepartureTime(), t2.getDepartureTime()).abs();
                    long idealHeadwayMinutes = 15;
                    long actualMinutes = gap.toMinutes();
                    return Math.abs(actualMinutes - idealHeadwayMinutes) > 5;
                })
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (t1, t2) -> {
                            Duration gap = Duration.between(t1.getDepartureTime(), t2.getDepartureTime()).abs();
                            long idealHeadwayMinutes = 15;
                            return (int) Math.abs(gap.toMinutes() - idealHeadwayMinutes);
                        })
                .asConstraint("Prefer even headways");
    }

    // ========== HELPER METHODS ==========

    /**
     * Compute the arrival time for a trip based on its route, direction, and travel times.
     */
    private LocalDateTime computeArrivalTime(ScheduledTrip trip) {
        if (trip.getDepartureTime() == null || trip.getAssignedTrain() == null || trip.getRoute() == null) {
            return null;
        }

        var stops = trip.getRoute().getStopsInDirection(trip.getDirection());
        LocalDateTime currentTime = trip.getDepartureTime();

        for (int i = 0; i < stops.size(); i++) {
            Station station = stops.get(i);

            // Add dwell time (except at last stop)
            if (i > 0 && i < stops.size() - 1) {
                currentTime = currentTime.plusSeconds(station.getStopDurationInSeconds());
            }

            // Add travel time to next station
            if (i < stops.size() - 1) {
                var conn = station.getConnectionTo(stops.get(i + 1));
                if (conn != null) {
                    int travelSeconds = trip.getAssignedTrain().getAdjustedTravelTime(conn.getBaseTravelTimeSeconds());
                    currentTime = currentTime.plusSeconds(travelSeconds);
                }
            }
        }

        return currentTime;
    }
}
