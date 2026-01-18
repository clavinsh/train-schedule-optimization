package com.example.solver;

import java.time.Duration;
import com.example.domain.Train;
import com.example.domain.Trip;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

public class TrainScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                // trainCapacity(constraintFactory),
                // arrivalBeforeDeparture(constraintFactory),
                // trainStartsAtDepo(constraintFactory),
                // trainEndsAtDepo(constraintFactory),
                // minIntervalBetweenTrains(constraintFactory),
                // Soft constraints
                // maximizePassengerSatisfaction(constraintFactory),
                // minimizeIdleTime(constraintFactory)
        };
    }

    // ========== HARD CONSTRAINTS ==========

    // A train's passenger capacity must not be exceeded.
    protected Constraint trainCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPassengerCount() > trip.getTrain().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        trip -> trip.getPassengerCount() - trip.getTrain().getCapacity())
                .asConstraint("Train capacity");
    }

    // A train cannot depart from a station before it has arrived.
    protected Constraint arrivalBeforeDeparture(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getArrivalTime() != null
                        && trip.getDepartureTime().isBefore(trip.getArrivalTime()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Arrival before departure");
    }
    
    // The first trip must start from the train's home depot.
    protected Constraint trainStartsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
            .filter(trip -> trip.getPreviousStandstill() instanceof Train) // This identifies the first trip in a chain
            .filter(trip -> !trip.getStation().equals(trip.getTrain().getHomeDepo().getStation()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Train must start at assigned depo");
    }

    // The last trip must end at the train's home depot.
    protected Constraint trainEndsAtDepo(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
            .filter(trip -> trip.getNextTrip() == null) // This identifies the last trip
            .filter(trip -> !trip.getStation().equals(trip.getTrain().getHomeDepo().getStation()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Train must end at assigned depo");
    }
    
    // A minimum time interval must be maintained between departures of different trains from the same station.
    protected Constraint minIntervalBetweenTrains(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Trip.class,
                        Joiners.equal(Trip::getStation),
                        Joiners.filtering((trip1, trip2) -> trip1.getTrain() != trip2.getTrain() 
                            && trip1.getDepartureTime() != null && trip2.getDepartureTime() != null))
                .filter((trip1, trip2) -> 
                        Duration.between(trip1.getDepartureTime(), trip2.getDepartureTime()).abs().toMinutes() < 5)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum interval between trains");
    }

    // ========== SOFT CONSTRAINTS ==========

    // Maximize passenger satisfaction by picking them up.
    protected Constraint maximizePassengerSatisfaction(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getDepartureTime() != null)
                .reward(HardSoftScore.ONE_SOFT,
                        trip -> {
                            int embarking = trip.getStation().getEmbarkingDemand()
                                    .getOrDefault(trip.getDepartureTime().getHour(), 0);
                            return Math.min(embarking, trip.getTrain().getCapacity() - (trip.getPassengerCount() - embarking));
                        })
                .asConstraint("Maximize passenger satisfaction");
    }

    // Minimize idle time between trips.
    protected Constraint minimizeIdleTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Trip.class)
                .filter(trip -> trip.getPreviousStandstill() != null && trip.getArrivalTime() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        trip -> {
                            Duration idleTime = Duration.between(trip.getPreviousStandstill().getDepartureTime(), trip.getArrivalTime());
                            long idleMinutes = idleTime.toMinutes();
                            if (idleMinutes > 30) { // Penalize idle time longer than 30 minutes
                                return (int) (idleMinutes - 30);
                            }
                            return 0;
                        })
                .asConstraint("Minimize idle time");
    }
}
