package org.acme.RollingStockRosteringOptimization.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.RollingStockRosteringOptimization.domain.Depo;
import org.acme.RollingStockRosteringOptimization.domain.Ride;
import org.acme.RollingStockRosteringOptimization.domain.Route;
import org.acme.RollingStockRosteringOptimization.domain.Train;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinct;

public class RollingStockSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                rideConflict(constraintFactory),
                capacityExceeded(constraintFactory),
                firstRideNotFromDepo(constraintFactory),
                lastRideNotToDepo(constraintFactory),
                routeStationCoveragePartial(constraintFactory),
                routeStationCoverageNone(constraintFactory),
                // Soft constraints
                maximizePassengersOnloaded(constraintFactory),
                minimizeTraveledDistance(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    /**
     * Same train cannot be assigned to overlapping rides.
     */
    public Constraint rideConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Ride.class,
                equal(Ride::getTrain),
                overlapping(Ride::getDepartureTime, Ride::getArrivalTime))
                .penalize(HardSoftLongScore.ofHard(10))
                .asConstraint("Ride conflict");
    }

    /**
     * Train cannot exceed its passenger capacity.
     */
    public Constraint capacityExceeded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Ride.class)
                .filter(Ride::exceedsCapacity)
                .penalize(HardSoftLongScore.ofHard(100))
                .asConstraint("Capacity exceeded");
    }

    /**
     * Train's first ride of the day must depart from a depot station.
     * Penalize if the first ride's departure station is not at any depot.
     */
    public Constraint firstRideNotFromDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Train.class)
                .join(Ride.class, equal(Function.identity(), Ride::getTrain))
                // Find the first ride for this train (no other ride with earlier departure)
                .ifNotExists(Ride.class,
                        equal((train, ride) -> train, Ride::getTrain),
                        greaterThan((train, ride) -> ride.getDepartureTime(), Ride::getDepartureTime))
                // Penalize if departure station is not at any depot
                .ifNotExists(Depo.class,
                        equal((train, ride) -> ride.getDepartureStation(), Depo::getStation))
                .penalize(HardSoftLongScore.ofHard(1))
                .asConstraint("First ride not departing from depot");
    }

    /**
     * Train's last ride of the day must arrive at a depot station.
     * Penalize if the last ride's arrival station is not at any depot.
     */
    public Constraint lastRideNotToDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Train.class)
                .join(Ride.class, equal(Function.identity(), Ride::getTrain))
                // Find the last ride for this train (no other ride with later departure)
                .ifNotExists(Ride.class,
                        equal((train, ride) -> train, Ride::getTrain),
                        lessThan((train, ride) -> ride.getDepartureTime(), Ride::getDepartureTime))
                // Penalize if arrival station is not at any depot
                .ifNotExists(Depo.class,
                        equal((train, ride) -> ride.getArrivalStation(), Depo::getStation))
                .penalize(HardSoftLongScore.ofHard(1))
                .asConstraint("Last ride not arriving at depot");
    }

    /**
     * Routes with partial coverage: penalize for each uncovered segment.
     * This handles routes that have some rides with trains but not all segments covered.
     */
    public Constraint routeStationCoveragePartial(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Route.class)
                .filter(route -> route.getSegmentCount() > 0)
                .join(Ride.class,
                        equal(Function.identity(), Ride::getRoute))
                .filter((route, ride) -> ride.getTrain() != null && ride.isValidRouteSegment())
                .groupBy((route, ride) -> route,
                        countDistinct((route, ride) -> ride.getRouteSegmentIndex()))
                .filter((route, coveredSegments) -> coveredSegments < route.getSegmentCount())
                .penalize(HardSoftLongScore.ofHard(1),
                        (route, coveredSegments) -> route.getSegmentCount() - coveredSegments)
                .asConstraint("Route station coverage - partial");
    }

    /**
     * Routes with no coverage: penalize all segments if no valid rides with trains are assigned.
     * This handles routes where either no rides exist, or all rides lack train assignments.
     */
    public Constraint routeStationCoverageNone(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Route.class)
                .filter(route -> route.getSegmentCount() > 0)
                .ifNotExists(Ride.class,
                        equal(Function.identity(), Ride::getRoute),
                        filtering((route, ride) -> ride.getTrain() != null && ride.isValidRouteSegment()))
                .penalize(HardSoftLongScore.ofHard(1), Route::getSegmentCount)
                .asConstraint("Route station coverage - none");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    /**
     * Maximize the number of passengers onloaded.
     * Reward each passenger that is transported.
     */
    public Constraint maximizePassengersOnloaded(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Ride.class)
                .filter(ride -> ride.getTrain() != null)
                .reward(HardSoftLongScore.ofSoft(1), Ride::getPassengerCount)
                .asConstraint("Maximize passengers onloaded");
    }

    /**
     * Minimize the total traveled distance.
     * Penalize each kilometer traveled.
     */
    public Constraint minimizeTraveledDistance(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Ride.class)
                .filter(ride -> ride.getTrain() != null && ride.getDistanceKm() != null)
                .penalize(HardSoftLongScore.ofSoft(1), ride -> ride.getDistanceKm().intValue())
                .asConstraint("Minimize traveled distance");
    }
}
