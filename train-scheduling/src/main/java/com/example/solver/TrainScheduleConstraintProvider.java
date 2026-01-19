package com.example.solver;

import java.time.Duration;

import com.example.domain.RouteDeparture;
import com.example.domain.Station;
import com.example.domain.TrainDepoAssignment;
import com.example.domain.Depo;
import com.example.domain.TrainConfiguration;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import java.util.function.Function;

/**
 * {@link ConstraintProvider} for the Train Schedule Optimization problem.
 *
 * This class defines constraints for scheduling train trips (RouteDeparture entities).
 * Each RouteDeparture represents a complete trip running a route from start to end.
 * The solver assigns:
 * - Which train runs each trip
 * - What time the trip departs from the first station
 *
 * Constraints are categorized into:
 * - Hard Constraints: Must never be violated for a feasible solution
 * - Soft Constraints: Optimize solution quality
 */
public class TrainScheduleConstraintProvider implements ConstraintProvider {

    // Configuration constants (could be moved to TrainConfiguration)

    private static final double UNDERUTILIZATION_THRESHOLD = 0.2;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                trainEndsAtDepo(constraintFactory), // Vilciens sāk dienu DEPO
                trainStartsAtDepo(constraintFactory), // Vilciens beidz dienu tajā pašā DEPO kurā sāka
                noOverlappingTrips(constraintFactory), // Vienam vilcienam nevar būt laiku pārklājoši braucieni
                trainContinuity(constraintFactory), // Vilciens nevar "teleportēties", t.i., ja beidz stacijā Y, tad jābrauc no stacijas Y
                minIntervalBetweenDepartures(constraintFactory), // Starp braucieniem pa vienu maršrutu jābut noteiktai laika atstarpei starp vilcieniem
                                                                 // (negriba kolīzijas)
                trainCapacityNotExceeded(constraintFactory),  // Pārbaudam vai vilciena kapacitāte netiek pārsniegta ņemot vērā stacijas cilvēku pieprasījumu
                depotCapacityNotExceeded(constraintFactory), // Nedrīkstam pārsniegt DEPO kapacitāti

                // Soft constraints
                maximizePassengerPickup(constraintFactory), // Mēģinam savākt pēc iespējas vairāk pasažierus
                minimizeUnderutilizedTrips(constraintFactory) // Izvairamies no brauciniem ar mazu pasažieru skaitu
        };
    }

    // ========== HARD CONSTRAINTS ==========

    /**
     * Hard Constraint: The first trip of a train must start at its assigned depot.
     *
     * Uses ifNotExists to find trips with no earlier trip for the same train.
     */
    protected Constraint trainStartsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RouteDeparture.class)
                .filter(rd -> rd.getTrain() != null && rd.getDepartureTime() != null)
                // Find the first trip for this train (no earlier trip exists)
                .ifNotExists(RouteDeparture.class,
                        Joiners.equal(RouteDeparture::getTrain),
                        Joiners.lessThan(RouteDeparture::getDepartureTime))
                // Join with depot assignment
                .join(TrainDepoAssignment.class,
                        Joiners.equal(RouteDeparture::getTrain, TrainDepoAssignment::getTrain))
                // Check if first station matches depot
                .filter((rd, depoAssignment) -> {
                    Station firstStation = rd.getFirstStation();
                    Station depoStation = depoAssignment.getDepo().getStation();
                    return firstStation != null && !firstStation.equals(depoStation);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must start at assigned depot");
    }

    /**
     * Hard Constraint: The last trip of a train must end at its assigned depot.
     *
     * Uses ifNotExists to find trips with no later trip for the same train.
     */
    protected Constraint trainEndsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RouteDeparture.class)
                .filter(rd -> rd.getTrain() != null && rd.getDepartureTime() != null)
                // Find the last trip for this train (no later trip exists)
                .ifNotExists(RouteDeparture.class,
                        Joiners.equal(RouteDeparture::getTrain),
                        Joiners.greaterThan(RouteDeparture::getDepartureTime))
                // Join with depot assignment
                .join(TrainDepoAssignment.class,
                        Joiners.equal(RouteDeparture::getTrain, TrainDepoAssignment::getTrain))
                // Check if last station matches depot
                .filter((rd, depoAssignment) -> {
                    Station lastStation = rd.getLastStation();
                    Station depoStation = depoAssignment.getDepo().getStation();
                    return lastStation != null && !lastStation.equals(depoStation);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train must end at assigned depot");
    }

    /**
     * Hard Constraint: A train cannot have overlapping trips.
     *
     * Two trips for the same train must not overlap in time.
     */
    protected Constraint noOverlappingTrips(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(RouteDeparture.class,
                        Joiners.equal(RouteDeparture::getTrain),
                        Joiners.filtering((rd1, rd2) ->
                                rd1.getTrain() != null &&
                                rd1.getDepartureTime() != null &&
                                rd2.getDepartureTime() != null))
                .filter(RouteDeparture::overlaps)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No overlapping trips for same train");
    }

    /**
     * Hard Constraint: Consecutive trips must connect properly.
     *
     * For the same train, the last station of one trip must equal
     * the first station of the next trip (train can't teleport).
     */
    protected Constraint trainContinuity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(RouteDeparture.class,
                        Joiners.equal(RouteDeparture::getTrain))
                .filter((rd1, rd2) -> rd1.getTrain() != null &&
                        rd1.getDepartureTime() != null && rd2.getDepartureTime() != null)
                // Check only consecutive trips (no trip in between)
                .ifNotExists(RouteDeparture.class,
                        Joiners.equal((rd1, rd2) -> rd1.getTrain(), RouteDeparture::getTrain),
                        Joiners.filtering((rd1, rd2, middle) -> {
                            if (middle.getDepartureTime() == null) return false;
                            if (middle.equals(rd1) || middle.equals(rd2)) return false;

                            int rd1Min = toMinutes(rd1);
                            int rd2Min = toMinutes(rd2);
                            int middleMin = toMinutes(middle);

                            int earlier = Math.min(rd1Min, rd2Min);
                            int later = Math.max(rd1Min, rd2Min);

                            return middleMin > earlier && middleMin < later;
                        }))
                .filter((rd1, rd2) -> {
                    // Determine which is first chronologically
                    RouteDeparture first = toMinutes(rd1) <= toMinutes(rd2) ? rd1 : rd2;
                    RouteDeparture second = toMinutes(rd1) <= toMinutes(rd2) ? rd2 : rd1;

                    // Skip if trips overlap (handled by other constraint)
                    if (first.overlaps(second)) {
                        return false;
                    }

                    // Check if first trip can be followed by second
                    return !first.canBeFollowedBy(second);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Train continuity - trips must connect");
    }

    /**
     * Hard Constraint: Minimum interval between different trains at the same station.
     *
     * Prevents platform congestion by ensuring trains don't depart from
     * the same station within MIN_INTERVAL_MINUTES of each other.
     */
    protected Constraint minIntervalBetweenDepartures(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(RouteDeparture.class,
                        Joiners.equal(RouteDeparture::getFirstStation))
                .join(TrainConfiguration.class,
                        // Dummy joiner to bring in the single TrainConfiguration fact.
                        // We join on a constant, effectively joining all rd1 with the single config.
                        Joiners.equal((rd1, config) -> 1, config -> 1)) // Use ifExists to add the single TrainConfiguration fact
                .filter((RouteDeparture rd1, RouteDeparture rd2, TrainConfiguration config) -> {
                    if (rd1.getTrain() == null || rd2.getTrain() == null ||
                            rd1.getDepartureTime() == null || rd2.getDepartureTime() == null ||
                            config == null) {
                        return false;
                    }
                    if (rd1.getTrain().equals(rd2.getTrain())) {
                        return false;
                    }

                    Duration interval = Duration.between(
                            rd1.getDepartureTime(),
                            rd2.getDepartureTime()).abs();
                    return interval.toMinutes() < config.getMinIntervalBetweenTrains().toMinutes();
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum interval between departures");
    }

    /**
     * Hard Constraint: Train capacity must not be exceeded at any point during the trip.
     *
     * Uses the route-wide demand calculation to check maximum passenger load
     * across all stations on the route.
     */
    protected Constraint trainCapacityNotExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RouteDeparture.class)
                .filter(rd -> rd.getTrain() != null && rd.getDepartureTime() != null)
                .filter(RouteDeparture::exceedsCapacity)
                .penalize(HardSoftScore.ONE_HARD, RouteDeparture::getCapacityOverflow)
                .asConstraint("Train capacity not exceeded");
    }

    protected Constraint depotCapacityNotExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Depo.class)
                .join(TrainDepoAssignment.class,
                        Joiners.equal(Function.identity(), TrainDepoAssignment::getDepo))
                .groupBy((Depo depo, TrainDepoAssignment tda) -> depo,
                         ConstraintCollectors.countBi())
                .filter((depo, assignedTrainCount) -> assignedTrainCount > depo.getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        (depo, assignedTrainCount) -> assignedTrainCount - depo.getCapacity())
                .asConstraint("Depot capacity not exceeded");
    }

    // ========== SOFT CONSTRAINTS ==========

    /**
     * Soft Constraint: Maximize passenger pickup across ALL stations on the route.
     *
     * Rewards trips based on total embarking passengers across the entire route,
     * capped by train capacity.
     */
    protected Constraint maximizePassengerPickup(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RouteDeparture.class)
                .filter(rd -> rd.getTrain() != null && rd.getDepartureTime() != null)
                .reward(HardSoftScore.ONE_SOFT,
                        rd -> {
                            int totalPassengers = rd.getTotalEmbarkingPassengers();
                            int capacity = rd.getTrain().getCapacity();
                            // Reward for passengers picked up, capped by capacity
                            return Math.min(totalPassengers, capacity);
                        })
                .asConstraint("Maximize passenger pickup");
    }

    /**
     * Soft Constraint: Penalize underutilized trips.
     *
     * Discourages scheduling trips where total embarking passengers across
     * the route are less than UNDERUTILIZATION_THRESHOLD of train capacity.
     */
    protected Constraint minimizeUnderutilizedTrips(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RouteDeparture.class)
                .filter(rd -> rd.getTrain() != null && rd.getDepartureTime() != null)
                .filter(rd -> {
                    int totalPassengers = rd.getTotalEmbarkingPassengers();
                    int capacity = rd.getTrain().getCapacity();
                    return totalPassengers < capacity * UNDERUTILIZATION_THRESHOLD;
                })
                .penalize(HardSoftScore.ONE_SOFT,
                        rd -> rd.getTrain().getCapacity() - rd.getTotalEmbarkingPassengers())
                .asConstraint("Minimize underutilized trips");
    }

    // ========== HELPER METHODS ==========

    /**
     * Converts a RouteDeparture's time to minutes from midnight for comparison.
     */
    private static int toMinutes(RouteDeparture rd) {
        if (rd.getDepartureTime() == null) return 0;
        return rd.getDepartureTime().getHour() * 60 + rd.getDepartureTime().getMinute();
    }
}
