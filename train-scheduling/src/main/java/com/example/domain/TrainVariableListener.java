package com.example.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class TrainVariableListener implements VariableListener<RollingStockSchedule, Trip> {

    @Override
    public void beforeEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateTrain(scoreDirector, trip);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateTrain(scoreDirector, trip);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    private void updateTrain(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        Train train = computeTrain(trip);
        scoreDirector.beforeVariableChanged(trip, "train");
        trip.setTrain(train);
        scoreDirector.afterVariableChanged(trip, "train");

        // Propagate to next trips in the chain
        Trip currentTrip = trip.getNextTrip();
        while (currentTrip != null) {
            scoreDirector.beforeVariableChanged(currentTrip, "train");
            currentTrip.setTrain(train);
            scoreDirector.afterVariableChanged(currentTrip, "train");
            currentTrip = currentTrip.getNextTrip();
        }
    }

    private Train computeTrain(Trip trip) {
        Standstill previousStandstill = trip.getPreviousStandstill();
        if (previousStandstill == null) {
            return null;
        }
        if (previousStandstill instanceof Train) {
            return (Train) previousStandstill;
        }
        if (previousStandstill instanceof Trip) {
            return ((Trip) previousStandstill).getTrain();
        }
        return null;
    }
}
