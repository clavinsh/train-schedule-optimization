package com.example.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class NextTripVariableListener implements VariableListener<RollingStockSchedule, Trip> {

    @Override
    public void beforeEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateNextTrip(scoreDirector, trip);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Clear the old previousStandstill's nextTrip before change
        Standstill oldPreviousStandstill = trip.getPreviousStandstill();
        if (oldPreviousStandstill != null && oldPreviousStandstill.getNextTrip() == trip) {
            if (oldPreviousStandstill instanceof Trip) {
                scoreDirector.beforeVariableChanged(oldPreviousStandstill, "nextTrip");
                oldPreviousStandstill.setNextTrip(null);
                scoreDirector.afterVariableChanged(oldPreviousStandstill, "nextTrip");
            } else {
                // Train is not a planning entity, just update directly
                oldPreviousStandstill.setNextTrip(null);
            }
        }
    }

    @Override
    public void afterVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updateNextTrip(scoreDirector, trip);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Clear the previousStandstill's nextTrip before removal
        Standstill previousStandstill = trip.getPreviousStandstill();
        if (previousStandstill != null && previousStandstill.getNextTrip() == trip) {
            if (previousStandstill instanceof Trip) {
                scoreDirector.beforeVariableChanged(previousStandstill, "nextTrip");
                previousStandstill.setNextTrip(null);
                scoreDirector.afterVariableChanged(previousStandstill, "nextTrip");
            } else {
                // Train is not a planning entity, just update directly
                previousStandstill.setNextTrip(null);
            }
        }
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    private void updateNextTrip(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        Standstill previousStandstill = trip.getPreviousStandstill();
        if (previousStandstill != null) {
            if (previousStandstill instanceof Trip) {
                scoreDirector.beforeVariableChanged(previousStandstill, "nextTrip");
                previousStandstill.setNextTrip(trip);
                scoreDirector.afterVariableChanged(previousStandstill, "nextTrip");
            } else {
                // Train is not a planning entity, just update directly
                previousStandstill.setNextTrip(trip);
            }
        }
    }
}
