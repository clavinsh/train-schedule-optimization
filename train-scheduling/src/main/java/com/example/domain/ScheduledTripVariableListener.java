package com.example.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

/**
 * Variable listener that updates shadow variables when planning variables change.
 * Updates: arrivalTime, trackOccupancies
 */
public class ScheduledTripVariableListener implements VariableListener<TrainSchedule, ScheduledTrip> {

    @Override
    public void beforeEntityAdded(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        // Nothing to do
    }

    @Override
    public void afterEntityAdded(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        updateShadowVariables(scoreDirector, trip);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        // Nothing to do
    }

    @Override
    public void afterVariableChanged(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        updateShadowVariables(scoreDirector, trip);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        // Nothing to do
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        // Clear shadow variables
        scoreDirector.beforeVariableChanged(trip, "arrivalTime");
        trip.setArrivalTime(null);
        scoreDirector.afterVariableChanged(trip, "arrivalTime");

        scoreDirector.beforeVariableChanged(trip, "trackOccupancies");
        trip.setTrackOccupancies(Collections.emptyList());
        scoreDirector.afterVariableChanged(trip, "trackOccupancies");
    }

    private void updateShadowVariables(ScoreDirector<TrainSchedule> scoreDirector, ScheduledTrip trip) {
        LocalDateTime newArrivalTime = computeArrivalTime(trip);
        List<TrackOccupancy> newOccupancies = computeTrackOccupancies(trip);

        // Update arrivalTime
        scoreDirector.beforeVariableChanged(trip, "arrivalTime");
        trip.setArrivalTime(newArrivalTime);
        scoreDirector.afterVariableChanged(trip, "arrivalTime");

        // Update trackOccupancies
        scoreDirector.beforeVariableChanged(trip, "trackOccupancies");
        trip.setTrackOccupancies(newOccupancies);
        scoreDirector.afterVariableChanged(trip, "trackOccupancies");
    }

    private LocalDateTime computeArrivalTime(ScheduledTrip trip) {
        if (trip.getDepartureTime() == null || trip.getAssignedTrain() == null || trip.getRoute() == null) {
            return null;
        }

        List<Station> stops = trip.getRoute().getStopsInDirection(trip.getDirection());
        LocalDateTime currentTime = trip.getDepartureTime();

        for (int i = 0; i < stops.size(); i++) {
            Station station = stops.get(i);

            // Add dwell time (except at first and last stop)
            if (i > 0 && i < stops.size() - 1) {
                currentTime = currentTime.plusSeconds(station.getStopDurationInSeconds());
            }

            // Add travel time to next station
            if (i < stops.size() - 1) {
                Connection conn = station.getConnectionTo(stops.get(i + 1));
                if (conn != null) {
                    int travelSeconds = trip.getAssignedTrain().getAdjustedTravelTime(conn.getBaseTravelTimeSeconds());
                    currentTime = currentTime.plusSeconds(travelSeconds);
                }
            }
        }

        return currentTime;
    }

    private List<TrackOccupancy> computeTrackOccupancies(ScheduledTrip trip) {
        if (trip.getDepartureTime() == null || trip.getAssignedTrain() == null || trip.getRoute() == null) {
            return Collections.emptyList();
        }

        List<TrackOccupancy> occupancies = new ArrayList<>();
        List<Station> stops = trip.getRoute().getStopsInDirection(trip.getDirection());
        LocalDateTime currentTime = trip.getDepartureTime();

        for (int i = 0; i < stops.size() - 1; i++) {
            Station from = stops.get(i);
            Station to = stops.get(i + 1);
            Connection conn = from.getConnectionTo(to);

            if (conn == null) continue;

            // Add dwell time at station (except first)
            if (i > 0) {
                currentTime = currentTime.plusSeconds(from.getStopDurationInSeconds());
            }
            LocalDateTime entryTime = currentTime;

            int travelSeconds = trip.getAssignedTrain().getAdjustedTravelTime(conn.getBaseTravelTimeSeconds());
            LocalDateTime exitTime = entryTime.plusSeconds(travelSeconds);

            TrackOccupancy occupancy = new TrackOccupancy();
            occupancy.setId(trip.getId() * 100 + i); // Unique ID per trip segment
            occupancy.setConnection(conn);
            occupancy.setDirection(conn.getDirectionFrom(from));
            occupancy.setTrain(trip.getAssignedTrain());
            occupancy.setTrip(trip);
            occupancy.setEntryTime(entryTime);
            occupancy.setExitTime(exitTime);

            occupancies.add(occupancy);
            currentTime = exitTime;
        }

        return occupancies;
    }
}
