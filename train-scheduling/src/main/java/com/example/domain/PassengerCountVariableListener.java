package com.example.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class PassengerCountVariableListener implements VariableListener<RollingStockSchedule, Trip> {

    @Override
    public void beforeEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updatePassengerCount(scoreDirector, trip);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        updatePassengerCount(scoreDirector, trip);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        // Do nothing
    }

    private void updatePassengerCount(ScoreDirector<RollingStockSchedule> scoreDirector, Trip trip) {
        Integer passengerCount = computePassengerCount(trip);
        scoreDirector.beforeVariableChanged(trip, "passengerCount");
        trip.setPassengerCount(passengerCount);
        scoreDirector.afterVariableChanged(trip, "passengerCount");

        // Propagate to next trips in the chain
        Trip currentTrip = trip.getNextTrip();
        while (currentTrip != null) {
            Integer nextPassengerCount = computePassengerCount(currentTrip);
            scoreDirector.beforeVariableChanged(currentTrip, "passengerCount");
            currentTrip.setPassengerCount(nextPassengerCount);
            scoreDirector.afterVariableChanged(currentTrip, "passengerCount");
            currentTrip = currentTrip.getNextTrip();
        }
    }

    private Integer computePassengerCount(Trip trip) {
        Standstill previousStandstill = trip.getPreviousStandstill();
        if (previousStandstill == null) {
            return 0;
        }
        int currentPassengerCount;

        if (previousStandstill instanceof Train) {
            currentPassengerCount = 0; // Train starts empty from depot
        } else if (previousStandstill instanceof Trip) {
            Trip previousTrip = (Trip) previousStandstill;
            if (previousTrip.getPassengerCount() == null || previousTrip.getDepartureTime() == null) {
                return 0;
            }
            Integer disembarking = previousTrip.getStation().getDisembarkingDemand()
                    .getOrDefault(previousTrip.getDepartureTime().getHour(), 0);
            currentPassengerCount = previousTrip.getPassengerCount() - disembarking;
        } else {
            currentPassengerCount = 0;
        }

        if (trip.getDepartureTime() == null || trip.getStation() == null) {
            return currentPassengerCount;
        }
        Integer embarking = trip.getStation().getEmbarkingDemand()
                .getOrDefault(trip.getDepartureTime().getHour(), 0);
        currentPassengerCount += embarking;
        return currentPassengerCount;
    }
}
