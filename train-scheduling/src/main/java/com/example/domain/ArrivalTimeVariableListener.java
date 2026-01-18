package com.example.domain;

import java.time.Duration;
import java.time.LocalTime;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class ArrivalTimeVariableListener implements VariableListener<RollingStockSchedule, Trip> {

    @Override
    public void beforeEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateArrivalTime(scoreDirector, trip);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateArrivalTime(scoreDirector, trip);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    private void updateArrivalTime(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        LocalTime arrivalTime = computeArrivalTime(trip);
        scoreDirector.beforeVariableChanged(trip, "arrivalTime");
        trip.setArrivalTime(arrivalTime);
        scoreDirector.afterVariableChanged(trip, "arrivalTime");

        // Propagate to next trips in the chain
        Trip currentTrip = trip.getNextTrip();
        while (currentTrip != null) {
            LocalTime nextArrivalTime = computeArrivalTime(currentTrip);
            scoreDirector.beforeVariableChanged(currentTrip, "arrivalTime");
            currentTrip.setArrivalTime(nextArrivalTime);
            scoreDirector.afterVariableChanged(currentTrip, "arrivalTime");
            currentTrip = currentTrip.getNextTrip();
        }
    }

    private LocalTime computeArrivalTime(Trip trip) {
        Standstill previousStandstill = trip.getPreviousStandstill();
        if (previousStandstill == null) {
            return null;
        }
        LocalTime previousDepartureTime = previousStandstill.getDepartureTime();
        Station previousStation = previousStandstill.getStation();

        if (previousDepartureTime == null || previousStation == null) {
            return null;
        }
        if (previousStation.getTravelTimesToNeighbors() == null || trip.getStation() == null) {
            return null;
        }
        Duration travelTime = previousStation.getTravelTimesToNeighbors().get(trip.getStation().getId());
        if (travelTime == null) {
            return null;
        }
        return previousDepartureTime.plus(travelTime);
    }
}
