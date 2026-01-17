package com.example.solver;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import com.example.domain.DepartureTime;
import com.example.domain.Depo;
import com.example.domain.Station;
import com.example.domain.StationDemand;
import com.example.domain.Train;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/**
 * Defines the constraints for rolling stock scheduling optimization.
 * Implements the hard and soft constraints from the domain specification.
 */
public class TrainScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                trainMustBeAssigned(constraintFactory),
                timeMustBeAssigned(constraintFactory),
                trainCapacityNotExceeded(constraintFactory),
                trainEndsAtDepo(constraintFactory),
                minIntervalBetweenTrains(constraintFactory),
                trainRouteConsistency(constraintFactory),

                // Soft constraints
                maximizePassengerPickup(constraintFactory),
                minimizeEmptyTrainTrips(constraintFactory),
                preferOnTimeArrivals(constraintFactory)
        };
    }

    // ========== HARD CONSTRAINTS ==========

    /**
     * Every departure must have a train assigned.
     */
    protected Constraint trainMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must be assigned");
    }

    /**
     * Every departure must have a time assigned.
     */
    protected Constraint timeMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getDepartureTime() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Departure time must be assigned");
    }

    /**
     * Hard constraint: Train cannot exceed its passenger capacity.
     * Checks demand at the departure station only (simplified - full route tracking would be complex).
     * This is a soft approximation since passengers also disembark along the route.
     */
    protected Constraint trainCapacityNotExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation),
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute),
                        Joiners.filtering((departure, demand) ->
                                departure.getDepartureTime().getHour() == demand.getTime().getHour()))
                .filter((departure, demand) -> demand.getEmbarkingPassengers() > departure.getTrain().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        (departure, demand) -> demand.getEmbarkingPassengers() - departure.getTrain().getCapacity())
                .asConstraint("Train capacity not exceeded");
    }

    /**
     * Hard constraint: Train's last trip of the day must end at a depot.
     * For each train, only the trip with the latest departure time is checked.
     * That trip's route must end at a depot station.
     */
    protected Constraint trainEndsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null
                        && departure.getDepartureTime() != null
                        && departure.getRoute() != null)
                // Only penalize if there's no later trip for this train
                .ifNotExists(DepartureTime.class,
                        Joiners.equal(DepartureTime::getTrain),
                        Joiners.greaterThan(DepartureTime::getDepartureTime))
                // Now check if the route's last station is a depot
                .ifNotExists(Depo.class,
                        Joiners.filtering((departure, depo) -> {
                            List<Station> stations = departure.getRoute().getStations();
                            Station lastStation = stations.get(stations.size() - 1);
                            return lastStation.equals(depo.getStation());
                        }))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must end at depot");
    }

    /**
     * Hard constraint: Minimum interval between trains at the same station.
     * Two different trains cannot depart from the same station within the minimum interval.
     */
    protected Constraint minIntervalBetweenTrains(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(DepartureTime.class,
                        Joiners.equal(DepartureTime::getStation),
                        Joiners.filtering((d1, d2) -> d1.getTrain() != null && d2.getTrain() != null
                                && !d1.getTrain().equals(d2.getTrain())
                                && d1.getDepartureTime() != null && d2.getDepartureTime() != null))
                .filter((d1, d2) -> {
                    Duration interval = Duration.between(d1.getDepartureTime(), d2.getDepartureTime()).abs();
                    // Default to 5 minute minimum interval
                    return interval.toMinutes() < 5;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum interval between trains");
    }

    /**
     * Hard constraint: Same train cannot run overlapping trips.
     * A train assigned to two trips must have enough time gap between them
     * (based on estimated route duration).
     */
    protected Constraint trainRouteConsistency(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(DepartureTime.class,
                        Joiners.equal(DepartureTime::getTrain))
                .filter((d1, d2) -> d1.getTrain() != null
                        && d1.getDepartureTime() != null && d2.getDepartureTime() != null)
                .filter((d1, d2) -> {
                    // Estimate trip duration: ~3 minutes per station
                    int d1Stations = d1.getRoute().getStations().size();
                    int d2Stations = d2.getRoute().getStations().size();

                    LocalTime d1Start = d1.getDepartureTime();
                    LocalTime d1End = d1Start.plusMinutes(d1Stations * 3L);
                    LocalTime d2Start = d2.getDepartureTime();
                    LocalTime d2End = d2Start.plusMinutes(d2Stations * 3L);

                    // Check for overlap: trips overlap if one starts before the other ends
                    boolean d1BeforeD2 = !d1End.isAfter(d2Start);
                    boolean d2BeforeD1 = !d2End.isAfter(d1Start);

                    // Conflict if neither is completely before the other
                    return !d1BeforeD2 && !d2BeforeD1;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train route consistency");
    }

    // ========== SOFT CONSTRAINTS ==========

    /**
     * Soft constraint: Maximize passenger pickup at each stop.
     * Rewards departures that pick up more passengers based on station demand.
     */
    protected Constraint maximizePassengerPickup(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation),
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute))
                .filter((departure, demand) ->
                        departure.getDepartureTime().getHour() == demand.getTime().getHour())
                .reward(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> {
                            int availablePassengers = demand.getEmbarkingPassengers(departure.getDepartureTime());
                            int trainCapacity = departure.getTrain().getCapacity();
                            // Reward picking up passengers (up to capacity)
                            return Math.min(availablePassengers, trainCapacity);
                        })
                .asConstraint("Maximize passenger pickup");
    }

    /**
     * Soft constraint: Minimize empty train trips.
     * Penalizes trips departing when there's low demand at the departure station.
     */
    protected Constraint minimizeEmptyTrainTrips(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation),
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute),
                        Joiners.filtering((departure, demand) ->
                                departure.getDepartureTime().getHour() == demand.getTime().getHour()))
                .filter((departure, demand) -> {
                    int capacity = departure.getTrain().getCapacity();
                    int passengers = demand.getEmbarkingPassengers();
                    // Consider a trip underutilized if less than 20% of capacity in demand
                    return passengers < capacity * 0.2;
                })
                .penalize(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> departure.getTrain().getCapacity() - demand.getEmbarkingPassengers())
                .asConstraint("Minimize empty train trips");
    }

    /**
     * Soft constraint: Prefer on-time arrivals.
     * Rewards trains that depart at times matching high passenger demand.
     */
    protected Constraint preferOnTimeArrivals(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation),
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute))
                .filter((departure, demand) -> {
                    // Check if departure is within the demand hour
                    int departureHour = departure.getDepartureTime().getHour();
                    int demandHour = demand.getTime().getHour();
                    return departureHour == demandHour;
                })
                .reward(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> {
                            // Higher reward for arriving when more passengers are waiting
                            // Later in the hour means more passengers have accumulated
                            int minute = departure.getDepartureTime().getMinute();
                            int basePassengers = demand.getEmbarkingPassengers();
                            // Scale reward by how many passengers would have accumulated
                            return (basePassengers * (minute + 1)) / 60;
                        })
                .asConstraint("Prefer on-time arrivals");
    }
}
