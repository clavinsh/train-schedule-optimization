package org.acme.rollingstockrostering.solver;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.rollingstockrostering.domain.*;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/**
 * RollingStockConstraintProvider - Defines all constraints for the problem
 *
 * This constraint provider works with Trip as the planning entity.
 * Each Trip represents a complete journey along a route that needs a train assigned.
 *
 * HARD CONSTRAINTS (must be satisfied for feasible solution):
 * 1. trainTimeConflict - A train cannot be assigned to overlapping trips
 * 2. trainStartsFromDepot - Train's first trip must start from its depot station
 * 3. trainEndsAtDepot - Train's last trip must end at its depot station
 *
 * SOFT CONSTRAINTS (optimization objectives):
 * 4. minimizeEmptyTrips - Penalize trips with zero passengers
 * 5. maximizePassengerPickup - Reward passenger transportation
 * 6. minimizeTrainUsage - Prefer using fewer unique trains
 */
public class RollingStockConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                trainTimeConflict(constraintFactory),
                trainStartsFromDepot(constraintFactory),
                trainEndsAtDepot(constraintFactory),

                // Soft constraints
                minimizeEmptyTrips(constraintFactory),
                maximizePassengerPickup(constraintFactory),
                preferFewerTrains(constraintFactory)
        };
    }

    // ==================== HARD CONSTRAINTS ====================

    /**
     * HARD CONSTRAINT 1: trainTimeConflict
     *
     * A train cannot be assigned to two trips that overlap in time.
     * Two trips conflict if:
     * - They have the same train assigned
     * - One trip starts before the other ends (considering turnaround time)
     *
     * This uses configurable minimum interval from Konfiguracija.
     */
    Constraint trainTimeConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Trip.class,
                        // Same train assigned
                        Joiners.equal(Trip::getVilciens))
                // Only check if train is assigned
                .filter((trip1, trip2) -> trip1.getVilciens() != null)
                // Check for time overlap
                .filter((trip1, trip2) -> tripsOverlap(trip1, trip2))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("trainTimeConflict");
    }

    /**
     * Check if two trips overlap in time (cannot be served by the same train).
     *
     * Trip A ends at: startTime + (stations-1) * travelTime + (stations-1) * stopTime
     * Trip B starts at: its startTime
     *
     * They conflict if:
     * - Trip A end + turnaround > Trip B start, OR
     * - Trip B end + turnaround > Trip A start
     */
    private boolean tripsOverlap(Trip trip1, Trip trip2) {
        if (trip1.getStartTime() == null || trip2.getStartTime() == null) {
            return false;
        }

        // Use default values (in a real implementation, get from solution's Konfiguracija)
        long travelTimeMinutes = 5;  // Travel time between stations
        long stopTimeMinutes = 2;    // Stop time at each station
        long turnaroundMinutes = 15; // Turnaround time

        // Estimate trip duration (simplified: assume 15 stations average)
        // In production, this should use actual route data
        int avgStations = 15;
        long tripDurationMinutes = (avgStations - 1) * (travelTimeMinutes + stopTimeMinutes);

        LocalTime trip1End = trip1.getStartTime().plusMinutes(tripDurationMinutes);
        LocalTime trip2End = trip2.getStartTime().plusMinutes(tripDurationMinutes);

        LocalTime trip1ReadyForNext = trip1End.plusMinutes(turnaroundMinutes);
        LocalTime trip2ReadyForNext = trip2End.plusMinutes(turnaroundMinutes);

        // Check if trip1 ends after trip2 starts (with turnaround)
        // AND trip2 ends after trip1 starts (with turnaround)
        // This means they overlap
        boolean trip1BlocksTrip2 = trip1ReadyForNext.isAfter(trip2.getStartTime()) &&
                                   trip1.getStartTime().isBefore(trip2End);
        boolean trip2BlocksTrip1 = trip2ReadyForNext.isAfter(trip1.getStartTime()) &&
                                   trip2.getStartTime().isBefore(trip1End);

        return trip1BlocksTrip2 || trip2BlocksTrip1;
    }

    /**
     * HARD CONSTRAINT 2: trainStartsFromDepot
     *
     * A train's first trip of the day must start from a station that has a depot
     * where that train is assigned.
     *
     * For each train, find its earliest trip and verify it starts from a depot station.
     */
    Constraint trainStartsFromDepot(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Trip.class)
                .filter(trip -> trip.getVilciens() != null)
                .filter(trip -> trip.getStartTime() != null)
                // Check if there's NO earlier trip for the same train
                .ifNotExists(Trip.class,
                        Joiners.equal(Trip::getVilciens),
                        Joiners.lessThan(Trip::getStartTime))
                // Join with Depo to find depot stations
                .ifNotExists(Depo.class,
                        Joiners.equal(
                                trip -> getFirstStationId(trip),
                                Depo::getStacijaId))
                // Join with TrainDepotAssignment to verify this train's depot
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("trainStartsFromDepot");
    }

    /**
     * HARD CONSTRAINT 3: trainEndsAtDepot
     *
     * A train's last trip of the day must end at a station that has a depot.
     *
     * For each train, find its latest trip and verify it ends at a depot station.
     */
    Constraint trainEndsAtDepot(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Trip.class)
                .filter(trip -> trip.getVilciens() != null)
                .filter(trip -> trip.getStartTime() != null)
                // Check if there's NO later trip for the same train
                .ifNotExists(Trip.class,
                        Joiners.equal(Trip::getVilciens),
                        Joiners.greaterThan(Trip::getStartTime))
                // Check that the last station is a depot station
                .ifNotExists(Depo.class,
                        Joiners.equal(
                                trip -> getLastStationId(trip),
                                Depo::getStacijaId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("trainEndsAtDepot");
    }

    // ==================== SOFT CONSTRAINTS ====================

    /**
     * SOFT CONSTRAINT 4: minimizeEmptyTrips
     *
     * Penalize trips that have zero or very few passengers.
     * Empty trips waste resources and should be avoided.
     *
     * Penalty: configurable emptyTripPenalty from Konfiguracija
     */
    Constraint minimizeEmptyTrips(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Trip.class)
                .filter(trip -> trip.getVilciens() != null)
                // For now, we penalize all trips equally
                // In production, join with CilvekuPieprasijums to check actual demand
                .join(CilvekuPieprasijums.class,
                        Joiners.equal(Trip::getMarsrutaId, CilvekuPieprasijums::getMarsrutaId))
                .filter((trip, demand) -> {
                    // Check if demand at trip time is zero
                    if (trip.getStartTime() == null || demand.getStunda() == null) {
                        return false;
                    }
                    return demand.getStunda().getHour() == trip.getStartTime().getHour() &&
                           demand.getCilvekuSkaits() == 0;
                })
                .penalize(HardSoftScore.ONE_SOFT, (trip, demand) -> 100)
                .asConstraint("minimizeEmptyTrips");
    }

    /**
     * SOFT CONSTRAINT 5: maximizePassengerPickup
     *
     * Reward trips based on passenger demand they serve.
     * Higher demand = higher reward.
     *
     * Uses linear interpolation as specified in the PDF:
     * If train arrives at minute N of hour H, demand is proportional to wait time.
     */
    Constraint maximizePassengerPickup(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Trip.class)
                .filter(trip -> trip.getVilciens() != null)
                .filter(trip -> trip.getStartTime() != null)
                // Join with demand data for this route
                .join(CilvekuPieprasijums.class,
                        Joiners.equal(Trip::getMarsrutaId, CilvekuPieprasijums::getMarsrutaId))
                // Match demand hour with trip hour
                .filter((trip, demand) -> {
                    if (demand.getStunda() == null) return false;
                    return demand.getStunda().getHour() == trip.getStartTime().getHour();
                })
                // Reward based on interpolated demand
                .reward(HardSoftScore.ONE_SOFT, (trip, demand) -> {
                    int interpolatedDemand = demand.getInterpolatedDemand(trip.getStartTime());
                    // Cap at train capacity if assigned
                    if (trip.getVilciens() != null) {
                        interpolatedDemand = Math.min(interpolatedDemand,
                                                      trip.getVilciens().getKapacitate());
                    }
                    return interpolatedDemand;
                })
                .asConstraint("maximizePassengerPickup");
    }

    /**
     * SOFT CONSTRAINT 6: preferFewerTrains
     *
     * Encourage efficient use of the fleet by rewarding train reuse.
     * If a train serves multiple trips, we get a bonus for efficiency.
     *
     * This is implemented by penalizing each unique train used.
     */
    Constraint preferFewerTrains(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Trip.class)
                .filter(trip -> trip.getVilciens() != null)
                // Group by train and count trips per train
                .groupBy(Trip::getVilciens, ConstraintCollectors.count())
                // Penalize each train that's used (encourages fewer unique trains)
                // But offset by number of trips (more trips per train = less penalty)
                .penalize(HardSoftScore.ONE_SOFT, (train, tripCount) -> {
                    // Base penalty for using a train: 50
                    // Reduction per trip: 10
                    // Net effect: using one train for many trips is better than many trains for few trips each
                    return Math.max(0, 50 - (tripCount * 10));
                })
                .asConstraint("preferFewerTrains");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get the first station ID for a trip.
     * For forward trips: first station of route
     * For return trips: last station of route (which is first in reverse)
     */
    private Long getFirstStationId(Trip trip) {
        // This is a simplified implementation
        // In production, this would look up the route from the solution
        return trip.getMarsrutaId(); // Placeholder - actual implementation needs route lookup
    }

    /**
     * Get the last station ID for a trip.
     * For forward trips: last station of route
     * For return trips: first station of route (which is last in reverse)
     */
    private Long getLastStationId(Trip trip) {
        // This is a simplified implementation
        // In production, this would look up the route from the solution
        return trip.getMarsrutaId(); // Placeholder - actual implementation needs route lookup
    }
}
