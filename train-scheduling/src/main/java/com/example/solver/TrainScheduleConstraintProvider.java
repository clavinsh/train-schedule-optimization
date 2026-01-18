package com.example.solver;

import java.time.Duration;

import com.example.domain.Depo;
import com.example.domain.Station;
import com.example.domain.Train;
import com.example.domain.TrainConfiguration;
import com.example.domain.Trip;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

public class TrainScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                trainCapacity(constraintFactory),
                trainStartsAtDepo(constraintFactory),
                trainEndsAtDepo(constraintFactory),
                minIntervalBetweenTrains(constraintFactory),
                routeSequence(constraintFactory),
                // arrivalBeforeDeparture(constraintFactory),
                // stationConnectivity(constraintFactory),
                // minimumDwellTime(constraintFactory),
                // depoCapacity(constraintFactory),
                
                // Soft constraints
                balanceTrainWorkload(constraintFactory),
                // maximizePassengerSatisfaction(constraintFactory),
                // minimizeDeadheading(constraintFactory),
                // rushHourPriority(constraintFactory),
                minimizeIdleTime(constraintFactory)
        };
    }

    // ========== HARD CONSTRAINTS ==========

    /**
     * A train's passenger capacity must not be exceeded.
     */
    protected Constraint trainCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getTrain() != null)
                .filter(trip -> trip.getPassengerCount() != null)
                .filter(trip -> trip.getPassengerCount() > trip.getTrain().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        trip -> trip.getPassengerCount() - trip.getTrain().getCapacity())
                .asConstraint("Train capacity");
    }

    /**
     * A train cannot depart from a station before it has arrived.
     */
    protected Constraint arrivalBeforeDeparture(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getArrivalTime() != null)
                .filter(trip -> trip.getDepartureTime() != null)
                .filter(trip -> trip.getDepartureTime().isBefore(trip.getArrivalTime()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Arrival before departure");
    }

    /**
     * The first trip of a train must start from the train's home depot station.
     */
    protected Constraint trainStartsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() instanceof Train)
                .filter(trip -> trip.getTrain() != null)
                .filter(trip -> trip.getTrain().getHomeDepo() != null)
                .filter(trip -> trip.getStation() != null)
                .filter(trip -> !trip.getStation().equals(trip.getTrain().getHomeDepo().getStation()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must start at home depo");
    }

    /**
     * The last trip of a train must end at the train's home depot station.
     */
    protected Constraint trainEndsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getNextTrip() == null)
                .filter(trip -> trip.getTrain() != null)
                .filter(trip -> trip.getTrain().getHomeDepo() != null)
                .filter(trip -> trip.getStation() != null)
                .filter(trip -> !trip.getStation().equals(trip.getTrain().getHomeDepo().getStation()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must end at home depo");
    }

    /**
     * A minimum time interval must be maintained between departures of different trains from the same station.
     */
    protected Constraint minIntervalBetweenTrains(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Trip.class,
                        Joiners.equal(Trip::getStation),
                        Joiners.filtering((trip1, trip2) ->
                                trip1.getTrain() != null && trip2.getTrain() != null
                                && !trip1.getTrain().equals(trip2.getTrain())
                                && trip1.getDepartureTime() != null
                                && trip2.getDepartureTime() != null))
                .filter((trip1, trip2) ->
                        Duration.between(trip1.getDepartureTime(), trip2.getDepartureTime()).abs().toMinutes() < 5)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum interval between trains");
    }

    /**
     * Consecutive trips on the same route must follow the correct station sequence.
     * If trip A is followed by trip B and both are on the same route:
     * - If A is not at the last station, B must be at the next station (stationIndex + 1)
     * - If A is at the last station, B must be at the first station (starting a new service)
     */
    protected Constraint routeSequence(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() instanceof Trip)
                .filter(trip -> {
                    Trip previousTrip = (Trip) trip.getPreviousStandstill();
                    if (previousTrip.getRoute() == null || trip.getRoute() == null) {
                        return false;
                    }
                    if (!trip.getRoute().equals(previousTrip.getRoute())) {
                        return false; // Different routes, no constraint
                    }

                    int prevIndex = previousTrip.getStationIndex();
                    int currIndex = trip.getStationIndex();
                    int routeSize = trip.getRoute().getStations().size();

                    if (prevIndex < routeSize - 1) {
                        // Previous was not at last station, current must be next
                        return currIndex != prevIndex + 1;
                    } else {
                        // Previous was at last station, current must be first (new service)
                        return currIndex != 0;
                    }
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Route sequence");
    }

    /**
     * There must be a valid travel path between consecutive trip stations.
     * The previous trip's station must have a defined travel time to the current trip's station.
     */
    protected Constraint stationConnectivity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() != null)
                .filter(trip -> trip.getPreviousStandstill().getStation() != null)
                .filter(trip -> trip.getStation() != null)
                .filter(trip -> {
                    Station prevStation = trip.getPreviousStandstill().getStation();
                    Station currStation = trip.getStation();

                    if (prevStation.equals(currStation)) {
                        return false; // Same station is always connected
                    }
                    if (prevStation.getTravelTimesToNeighbors() == null) {
                        return true; // No travel times defined, constraint violated
                    }
                    return prevStation.getTravelTimesToNeighbors().get(currStation.getId()) == null;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Station connectivity");
    }

    /**
     * Train must wait at least the configured station stop duration before departing.
     * departureTime must be >= arrivalTime + stationStopDuration
     */
    protected Constraint minimumDwellTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getArrivalTime() != null)
                .filter(trip -> trip.getDepartureTime() != null)
                .join(TrainConfiguration.class)
                .filter((trip, config) -> config.getStationStopDuration() != null)
                .filter((trip, config) -> {
                    Duration actualDwell = Duration.between(trip.getArrivalTime(), trip.getDepartureTime());
                    return actualDwell.compareTo(config.getStationStopDuration()) < 0;
                })
                .penalize(HardSoftScore.ONE_HARD, (trip, config) -> {
                    Duration actualDwell = Duration.between(trip.getArrivalTime(), trip.getDepartureTime());
                    Duration shortage = config.getStationStopDuration().minus(actualDwell);
                    return (int) Math.max(1, shortage.toMinutes());
                })
                .asConstraint("Minimum dwell time");
    }

    /**
     * The number of trains assigned to a depo must not exceed its capacity.
     */
    protected Constraint depoCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Train.class)
                .filter(train -> train.getHomeDepo() != null)
                .groupBy(Train::getHomeDepo, ConstraintCollectors.count())
                .filter((depo, count) -> count > depo.getCapacity())
                .penalize(HardSoftScore.ONE_HARD, (depo, count) -> count - depo.getCapacity())
                .asConstraint("Depo capacity");
    }

    // ========== SOFT CONSTRAINTS ==========

    /**
     * Balance workload across trains by penalizing the square of trip counts.
     * This naturally favors equal distribution of trips among trains.
     */
    protected Constraint balanceTrainWorkload(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getTrain() != null)
                .groupBy(Trip::getTrain, ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT, (train, count) -> count * count)
                .asConstraint("Balance train workload");
    }

    /**
     * Maximize passenger satisfaction by rewarding trips that pick up waiting passengers.
     */
    protected Constraint maximizePassengerSatisfaction(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getDepartureTime() != null)
                .filter(trip -> trip.getTrain() != null)
                .filter(trip -> trip.getStation() != null)
                .filter(trip -> trip.getStation().getEmbarkingDemand() != null)
                .filter(trip -> trip.getPassengerCount() != null)
                .reward(HardSoftScore.ONE_SOFT,
                        trip -> {
                            int hour = trip.getDepartureTime().getHour();
                            int embarking = trip.getStation().getEmbarkingDemand().getOrDefault(hour, 0);
                            int availableCapacity = trip.getTrain().getCapacity() - trip.getPassengerCount();
                            return Math.max(0, Math.min(embarking, availableCapacity));
                        })
                .asConstraint("Maximize passenger satisfaction");
    }

    /**
     * Minimize deadheading (empty train movements between different routes).
     * Penalizes when a train travels from one route to a different route.
     */
    protected Constraint minimizeDeadheading(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() instanceof Trip)
                .filter(trip -> {
                    Trip previousTrip = (Trip) trip.getPreviousStandstill();
                    if (previousTrip.getRoute() == null || trip.getRoute() == null) {
                        return false;
                    }
                    // Only penalize if routes are different
                    return !previousTrip.getRoute().equals(trip.getRoute());
                })
                .filter(trip -> {
                    Trip previousTrip = (Trip) trip.getPreviousStandstill();
                    Station prevStation = previousTrip.getStation();
                    Station currStation = trip.getStation();
                    if (prevStation == null || currStation == null) {
                        return false;
                    }
                    // Only penalize if stations are different (actual deadheading movement)
                    return !prevStation.equals(currStation);
                })
                .penalize(HardSoftScore.ONE_SOFT, trip -> {
                    Trip previousTrip = (Trip) trip.getPreviousStandstill();
                    Station prevStation = previousTrip.getStation();
                    Station currStation = trip.getStation();

                    if (prevStation.getTravelTimesToNeighbors() == null) {
                        return 30; // Default penalty if no travel time data
                    }
                    Duration travelTime = prevStation.getTravelTimesToNeighbors().get(currStation.getId());
                    if (travelTime == null) {
                        return 30; // Default penalty
                    }
                    return (int) travelTime.toMinutes();
                })
                .asConstraint("Minimize deadheading");
    }

    /**
     * Reward trips during rush hours (7-9 AM and 5-7 PM) to ensure good service coverage.
     */
    protected Constraint rushHourPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getDepartureTime() != null)
                .filter(trip -> {
                    int hour = trip.getDepartureTime().getHour();
                    return (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
                })
                .reward(HardSoftScore.ONE_SOFT, trip -> 10)
                .asConstraint("Rush hour priority");
    }

    /**
     * Minimize idle time between trips. Penalizes waiting time longer than 30 minutes.
     */
    protected Constraint minimizeIdleTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() != null)
                .filter(trip -> trip.getPreviousStandstill().getDepartureTime() != null)
                .filter(trip -> trip.getArrivalTime() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        trip -> {
                            Duration idleTime = Duration.between(
                                    trip.getPreviousStandstill().getDepartureTime(),
                                    trip.getArrivalTime());
                            long idleMinutes = idleTime.toMinutes();
                            if (idleMinutes > 30) {
                                return (int) (idleMinutes - 30);
                            }
                            return 0;
                        })
                .asConstraint("Minimize idle time");
    }
}
