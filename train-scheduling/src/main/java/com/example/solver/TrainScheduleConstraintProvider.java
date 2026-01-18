package com.example.solver;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import com.example.domain.DepartureTime;
import com.example.domain.Depo;
import com.example.domain.Station;
import com.example.domain.StationDemand;
import com.example.domain.Train;
import com.example.domain.TrainDepoAssignment;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/**
 * {@link ConstraintProvider} for the Train Schedule Optimization problem.
 * This class defines all the hard and soft constraints that guide the Timefold solver
 * in finding an optimal train schedule.
 * <p>
 * Constraints are categorized into:
 * <ul>
 *     <li><b>Hard Constraints:</b> Must never be violated. Violations result in an infeasible solution.</li>
 *     <li><b>Soft Constraints:</b> Should be optimized as much as possible. Violations reduce the quality
 *         of the solution but do not make it infeasible.</li>
 * </ul>
 * <p>
 * The constraints are implemented using the Timefold Constraint Streams API.
 */
public class TrainScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints: These constraints MUST NOT be violated for a solution to be feasible.
                // Each violation of a hard constraint adds a penalty that makes the solution less desirable.
                trainMustBeAssigned(constraintFactory),
                timeMustBeAssigned(constraintFactory),
                trainCapacityNotExceeded(constraintFactory),
                trainEndsAtDepo(constraintFactory),
                trainStartsAtDepo(constraintFactory),
                minIntervalBetweenTrains(constraintFactory),
                trainRouteConsistency(constraintFactory),

                // Soft constraints: These constraints represent preferences; violating them reduces
                // the quality of the solution but does not make it infeasible. The solver tries
                // to minimize penalties from soft constraints to find the best possible solution.
                maximizePassengerPickup(constraintFactory),
                minimizeEmptyTrainTrips(constraintFactory),
                preferOnTimeArrivals(constraintFactory)
        };
    }

    // ========== HARD CONSTRAINTS: Ensure feasibility of the schedule ==========

    /**
     * **Hard Constraint:** A train must be assigned to every scheduled departure time.
     * This constraint ensures that no {@link DepartureTime} planning entity is left
     * unassigned to a {@link Train}.
     * <p>
     * **Justification:** An unassigned departure time represents an infeasible schedule,
     * as a departure cannot occur without a physical train.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each {@code DepartureTime} that does not have a {@code Train} assigned.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} ensuring all departures have an assigned train.
     */
    protected Constraint trainMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() == null) // Find every DepartureTime that lacks a Train.
                .penalize(HardSoftScore.ONE_HARD) // Apply a hard penalty for each such occurrence.
                .asConstraint("Train must be assigned");
    }

    /**
     * **Hard Constraint:** A departure time must be assigned to every scheduled departure.
     * This constraint ensures that every {@link DepartureTime} planning entity has a
     * concrete departure time ({@link LocalTime}) assigned.
     * <p>
     * **Justification:** A departure cannot be scheduled without a specific time.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each {@code DepartureTime} that does not have a {@code LocalTime} assigned.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} ensuring all departures have a specific time assigned.
     */
    protected Constraint timeMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getDepartureTime() == null) // Find every DepartureTime that lacks a specific departure time.
                .penalize(HardSoftScore.ONE_HARD) // Apply a hard penalty for each such occurrence.
                .asConstraint("Departure time must be assigned");
    }

    /**
     * **Hard Constraint:** A train's passenger capacity must not be exceeded at its departure station.
     * This constraint ensures that the number of embarking passengers at a train's departure station
     * does not exceed the {@link Train}'s maximum capacity.
     * <p>
     * **Implementation Detail:** This check is a simplification. It only considers the demand at
     * the departure station at the departure hour. A more complex model would track passenger
     * load throughout the entire route, accounting for passengers disembarking at intermediate stations.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each passenger exceeding the train's capacity
     * at the departure station.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} ensuring trains do not exceed their capacity at departure.
     */
    protected Constraint trainCapacityNotExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation), // Match by station
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute),     // Match by route
                        // Filter to match demand within the same hour as the departure
                        Joiners.filtering((departure, demand) ->
                                departure.getDepartureTime().getHour() == demand.getTime().getHour()))
                // Only consider cases where embarking passengers exceed train capacity
                .filter((departure, demand) -> demand.getEmbarkingPassengers() > departure.getTrain().getCapacity())
                // Penalize by the number of passengers exceeding capacity
                .penalize(HardSoftScore.ONE_HARD,
                        (departure, demand) -> demand.getEmbarkingPassengers() - departure.getTrain().getCapacity())
                .asConstraint("Train capacity not exceeded");
    }

    /**
     * **Hard Constraint:** The last trip of a {@link Train} for the day must terminate at its assigned depot.
     * This constraint ensures that trains are properly garaged at their designated {@link Depo} at the
     * end of their daily schedule.
     * <p>
     * **Implementation Detail:**
     * 1. It iterates through all {@link DepartureTime} instances.
     * 2. It uses `ifNotExists` to identify, for each train, the {@code DepartureTime} that represents
     *    its *last* trip of the day (i.e., there is no other trip for the same train with a later departure time).
     * 3. It then joins with {@link TrainDepoAssignment} to find the depot assigned to that specific train.
     * 4. Finally, it filters for cases where the last station of the trip's {@link Route}
     *    does *not* match the train's assigned depot station.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each train whose last trip does not end at its assigned depot.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} ensuring trains end their day at their assigned depot.
     */
    protected Constraint trainEndsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null
                        && departure.getDepartureTime() != null
                        && departure.getRoute() != null)
                // Filter for the *last* trip of the day for each train:
                // only penalize if there's no later trip for this train
                .ifNotExists(DepartureTime.class,
                        Joiners.equal(DepartureTime::getTrain), // Same train
                        Joiners.greaterThan(DepartureTime::getDepartureTime)) // Later departure time
                // Join with the train's assigned depo
                .join(TrainDepoAssignment.class,
                        Joiners.equal(departure -> departure.getTrain(), TrainDepoAssignment::getTrain))
                // Filter where the route's last station is NOT the assigned depo station
                .filter((departure, trainDepoAssignment) -> {
                    List<Station> stations = departure.getRoute().getStations();
                    Station lastStation = stations.get(stations.size() - 1); // Get the last station of the trip's route
                    return !lastStation.equals(trainDepoAssignment.getDepo().getStation());
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must end at assigned depo");
    }

    /**
     * **Hard Constraint:** A minimum time interval must be maintained between departures of different trains
     * from the same station.
     * This constraint prevents two distinct {@link Train}s from departing from the same {@link Station}
     * within a specified minimum time buffer, ensuring operational safety and preventing platform congestion.
     * <p>
     * **Implementation Detail:**
     * 1. It uses `forEachUniquePair` to compare every unique pair of {@link DepartureTime} instances.
     * 2. Pairs are initially matched if they share the same {@link Station}.
     * 3. Further filtering ensures both departures have assigned trains and times, and that the trains are different.
     * 4. It calculates the absolute duration between the departure times of the two trains.
     * 5. It penalizes if this duration is less than a predefined minimum interval (currently 5 minutes).
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each pair of trains violating the minimum interval rule.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} enforcing minimum interval between trains at the same station.
     */
    protected Constraint minIntervalBetweenTrains(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(DepartureTime.class,
                        Joiners.equal(DepartureTime::getStation), // Match pairs of departures at the same station
                        // Filter to ensure both departures have assigned trains and times, and are different trains
                        Joiners.filtering((d1, d2) -> d1.getTrain() != null && d2.getTrain() != null
                                && !d1.getTrain().equals(d2.getTrain()) // Ensure different trains
                                && d1.getDepartureTime() != null && d2.getDepartureTime() != null))
                .filter((d1, d2) -> {
                    Duration interval = Duration.between(d1.getDepartureTime(), d2.getDepartureTime()).abs();
                    // Penalize if the interval is less than 5 minutes
                    return interval.toMinutes() < 5;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum interval between trains");
    }

    /**
     * **Hard Constraint:** A single {@link Train} cannot have overlapping trips.
     * This constraint ensures that a train, once assigned to a {@link DepartureTime},
     * has sufficient time to complete its current route before starting another assigned route.
     * <p>
     * **Implementation Detail:**
     * 1. It uses `forEachUniquePair` to compare every unique pair of {@link DepartureTime} instances
     *    assigned to the same {@link Train}.
     * 2. It filters for pairs where both departures have assigned trains and times.
     * 3. For each trip, it estimates its duration by multiplying the number of stations in the route
     *    by a fixed duration (3 minutes per station). This is a simplified estimation.
     * 4. It then checks for any overlap between the estimated start and end times of the two trips.
     * 5. A conflict is identified if neither trip is completed entirely before the other begins.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each pair of overlapping trips for the same train.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} preventing overlapping trips for the same train.
     */
    protected Constraint trainRouteConsistency(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(DepartureTime.class,
                        Joiners.equal(DepartureTime::getTrain)) // Match pairs of departures assigned to the same train
                .filter((d1, d2) -> d1.getTrain() != null
                        && d1.getDepartureTime() != null && d2.getDepartureTime() != null)
                .filter((d1, d2) -> {
                    // Estimate trip duration: A simplified estimation of 3 minutes per station.
                    // This could be replaced with a more accurate lookup based on actual route data.
                    int d1Stations = d1.getRoute().getStations().size();
                    int d2Stations = d2.getRoute().getStations().size();

                    LocalTime d1Start = d1.getDepartureTime();
                    LocalTime d1End = d1Start.plusMinutes(d1Stations * 3L); // Estimated end time for trip 1
                    LocalTime d2Start = d2.getDepartureTime();
                    LocalTime d2End = d2Start.plusMinutes(d2Stations * 3L); // Estimated end time for trip 2

                    // Check for overlap: Two trips overlap if one starts before the other ends,
                    // and vice versa. No overlap if one trip entirely precedes the other.
                    boolean d1BeforeD2 = !d1End.isAfter(d2Start); // True if d1 ends before or at d2 starts
                    boolean d2BeforeD1 = !d2End.isAfter(d1Start); // True if d2 ends before or at d1 starts

                    // Conflict if neither trip is entirely before the other (i.e., they overlap)
                    return !d1BeforeD2 && !d2BeforeD1;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train route consistency");
    }

    /**
     * **Hard Constraint:** The first trip of a {@link Train} for the day must originate from its assigned depot.
     * This constraint ensures that trains begin their daily service from their designated {@link Depo}.
     * <p>
     * **Implementation Detail:**
     * 1. It iterates through all {@link DepartureTime} instances.
     * 2. It uses `ifNotExists` to identify, for each train, the {@code DepartureTime} that represents
     *    its *first* trip of the day (i.e., there is no other trip for the same train with an earlier departure time).
     * 3. It then joins with {@link TrainDepoAssignment} to find the depot assigned to that specific train.
     * 4. Finally, it filters for cases where the first station of the trip's {@link Route}
     *    does *not* match the train's assigned depot station.
     * </p>
     * **Scoring:** Penalizes {@code 1 Hard} for each train whose first trip does not start at its assigned depot.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} ensuring trains start their day at their assigned depot.
     */
    protected Constraint trainStartsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null
                        && departure.getDepartureTime() != null
                        && departure.getRoute() != null)
                // Filter for the *first* trip of the day for each train:
                // only consider if there's no earlier trip for this train
                .ifNotExists(DepartureTime.class,
                        Joiners.equal(DepartureTime::getTrain), // Same train
                        Joiners.lessThan(DepartureTime::getDepartureTime)) // Earlier departure time
                // Join with the train's assigned depo
                .join(TrainDepoAssignment.class,
                        Joiners.equal(departureTime -> departureTime.getTrain(), TrainDepoAssignment::getTrain))
                // Filter where the route's first station is NOT the assigned depo station
                .filter((departure, trainDepoAssignment) -> {
                    Station firstStationOfRoute = departure.getRoute().getStations().get(0); // Get the first station of the trip's route
                    return !firstStationOfRoute.equals(trainDepoAssignment.getDepo().getStation());
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must start at assigned depo");
    }

    // ========== SOFT CONSTRAINTS ==========

    /**
     * **Soft Constraint:** Maximize the number of passengers picked up at each departure.
     * This constraint rewards {@link DepartureTime} assignments that correspond to
     * high passenger demand at the departure station, up to the train's capacity.
     * The goal is to optimize the schedule to serve as many embarking passengers as possible.
     * <p>
     * **Implementation Detail:**
     * 1. It iterates through all {@link DepartureTime} instances with an assigned train and time.
     * 2. It joins each {@code DepartureTime} with {@link StationDemand} based on the matching
     *    station and route, and ensuring the demand's hour matches the departure's hour.
     * 3. For each matching departure and demand, it calculates a reward. The reward is the
     *    minimum of the available embarking passengers and the train's capacity, ensuring
     *    that we only reward for passengers that can actually be transported.
     * </p>
     * **Scoring:** Rewards {@code 1 Soft} for each passenger picked up, up to the train's capacity.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} rewarding efficient passenger pickup.
     */
    protected Constraint maximizePassengerPickup(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation), // Match by station
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute))     // Match by route
                .filter((departure, demand) ->
                        // Ensure the demand hour matches the departure hour
                        departure.getDepartureTime().getHour() == demand.getTime().getHour())
                .reward(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> {
                            int availablePassengers = demand.getEmbarkingPassengers(departure.getDepartureTime());
                            int trainCapacity = departure.getTrain().getCapacity();
                            // Reward picking up passengers, capped by the train's capacity.
                            // We can't pick up more passengers than available or than the train can hold.
                            return Math.min(availablePassengers, trainCapacity);
                        })
                .asConstraint("Maximize passenger pickup");
    }

    /**
     * **Soft Constraint:** Minimize empty or underutilized train trips.
     * This constraint penalizes {@link DepartureTime} assignments where a {@link Train}
     * departs with significantly fewer passengers than its capacity, indicating an inefficient use
     * of resources. The goal is to avoid scheduling trains that are largely empty.
     * <p>
     * **Implementation Detail:**
     * 1. It iterates through all {@link DepartureTime} instances with an assigned train and time.
     * 2. It joins each {@code DepartureTime} with {@link StationDemand} based on matching
     *    station, route, and hour.
     * 3. It filters for cases where the number of embarking passengers is less than 20%
     *    of the train's total capacity, identifying "underutilized" trips.
     * 4. For each such underutilized trip, it applies a penalty proportional to the
     *    difference between the train's capacity and the actual embarking passengers.
     * </p>
     * **Scoring:** Penalizes {@code 1 Soft} for each unit of unused capacity in an underutilized train trip.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} penalizing empty or underutilized train trips.
     */
    protected Constraint minimizeEmptyTrainTrips(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation), // Match by station
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute),     // Match by route
                        // Filter to ensure the demand hour matches the departure hour
                        Joiners.filtering((departure, demand) ->
                                departure.getDepartureTime().getHour() == demand.getTime().getHour()))
                .filter((departure, demand) -> {
                    int capacity = departure.getTrain().getCapacity();
                    int passengers = demand.getEmbarkingPassengers();
                    // Identify underutilized trips: if embarking passengers are less than 20% of capacity.
                    return passengers < capacity * 0.2;
                })
                .penalize(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> departure.getTrain().getCapacity() - demand.getEmbarkingPassengers())
                .asConstraint("Minimize empty train trips");
    }

    /**
     * **Soft Constraint:** Prefer train departures that align with peak passenger demand.
     * This constraint rewards scheduling {@link DepartureTime}s such that trains depart
     * closer to when passenger demand has accumulated within a given hour. The goal is
     * to provide service when it's most needed by passengers.
     * <p>
     * **Implementation Detail:**
     * 1. It iterates through all {@link DepartureTime} instances with an assigned train and time.
     * 2. It joins each {@code DepartureTime} with {@link StationDemand} based on matching
     *    station and route.
     * 3. It filters to ensure the departure hour matches the demand hour.
     * 4. A reward is calculated based on the {@link StationDemand#getEmbarkingPassengers()}
     *    and the minute within the hour of the departure. The later in the hour a train departs,
     *    the higher the reward, assuming more passengers have accumulated over that hour.
     *    The reward is scaled by `(minute + 1) / 60` to represent the fraction of accumulated
     *    passengers within the hour.
     * </p>
     * **Scoring:** Rewards {@code 1 Soft} proportionally to the number of accumulated passengers
     * at the departure minute within the demand hour.
     *
     * @param constraintFactory The {@link ConstraintFactory} to build the constraint.
     * @return A {@link Constraint} rewarding departures that match peak passenger demand.
     */
    protected Constraint preferOnTimeArrivals(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DepartureTime.class)
                .filter(departure -> departure.getTrain() != null && departure.getDepartureTime() != null)
                .join(StationDemand.class,
                        Joiners.equal(DepartureTime::getStation, StationDemand::getStation), // Match by station
                        Joiners.equal(DepartureTime::getRoute, StationDemand::getRoute))     // Match by route
                .filter((departure, demand) -> {
                    // Check if departure is within the demand hour
                    int departureHour = departure.getDepartureTime().getHour();
                    int demandHour = demand.getTime().getHour();
                    return departureHour == demandHour;
                })
                .reward(HardSoftScore.ONE_SOFT,
                        (departure, demand) -> {
                            // Higher reward for departing later in the hour when more passengers
                            // would have accumulated based on the hourly demand.
                            int minute = departure.getDepartureTime().getMinute();
                            int basePassengers = demand.getEmbarkingPassengers();
                            // Scale reward by how many passengers would have accumulated up to that minute.
                            // Adding 1 to minute to avoid zero reward at minute 0.
                            return (basePassengers * (minute + 1)) / 60;
                        })
                .asConstraint("Prefer on-time arrivals");
    }
}
