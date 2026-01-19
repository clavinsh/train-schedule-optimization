package com.example.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTrip implements AbstractTrip {
    @PlanningId
    private int id;

    private Route route;
    private Direction direction;

    @PlanningVariable(valueRangeProviderRefs = "trainRange")
    private Train assignedTrain;

    // Departure time from the first station in route
    @PlanningVariable(valueRangeProviderRefs = "departureTimeRange")
    private LocalDateTime departureTime;

    // Shadow variables - automatically updated when planning variables change
    private List<StationVisit> stationVisits;

    @ShadowVariable(variableListenerClass = ScheduledTripVariableListener.class,
            sourceVariableName = "departureTime")
    @ShadowVariable(variableListenerClass = ScheduledTripVariableListener.class,
            sourceVariableName = "assignedTrain")
    private LocalDateTime arrivalTime;

    @ShadowVariable(variableListenerClass = ScheduledTripVariableListener.class,
            sourceVariableName = "departureTime")
    @ShadowVariable(variableListenerClass = ScheduledTripVariableListener.class,
            sourceVariableName = "assignedTrain")
    private List<TrackOccupancy> trackOccupancies;

    /**
     * Compute station visits based on current planning variable values.
     * Call this method to get station visits for a trip.
     */
    public List<StationVisit> computeStationVisits() {
        if (departureTime == null || assignedTrain == null) {
            return Collections.emptyList();
        }

        List<StationVisit> visits = new ArrayList<>();
        List<Station> stops = route.getStopsInDirection(direction);
        LocalDateTime currentTime = departureTime;

        for (int i = 0; i < stops.size(); i++) {
            Station station = stops.get(i);

            StationVisit visit = new StationVisit();
            visit.setTrip(this);
            visit.setStation(station);
            visit.setSequenceIdx(i);
            visit.setArrivalTime(currentTime);


            int dwellSeconds = 0;

            // Dwell time (0 at final stop)
            if (i != stops.size() - 1) {
                dwellSeconds = station.getStopDurationInSeconds();
            }

            visit.setDepartureTime(currentTime.plusSeconds(dwellSeconds));
            visits.add(visit);

            // Travel to next station
            if (i < stops.size() - 1) {
                Connection conn = station.getConnectionTo(stops.get(i + 1));
                int travelSeconds =
                        assignedTrain.getAdjustedTravelTime(conn.getBaseTravelTimeSeconds());
                currentTime = visit.getDepartureTime().plusSeconds(travelSeconds);
            }
        }

        return visits;
    }

    /**
     * Compute arrival time at the last station.
     */
    public LocalDateTime computeArrivalTime() {
        if (stationVisits == null || stationVisits.isEmpty()) {
            return null;
        }
        return stationVisits.get(stationVisits.size() - 1).getArrivalTime();
    }


    /**
     * Compute track occupancies for this trip.
     */
    public List<TrackOccupancy> computeTrackOccupancies() {
        if (departureTime == null || assignedTrain == null) {
            return Collections.emptyList();
        }

        List<TrackOccupancy> occupancies = new ArrayList<>();
        List<Station> stops = route.getStopsInDirection(direction);
        LocalDateTime currentTime = departureTime;

        for (int i = 0; i < stops.size() - 1; i++) {
            Station from = stops.get(i);
            Station to = stops.get(i + 1);
            Connection conn = from.getConnectionTo(to);

            // Skip dwell time at station
            currentTime = currentTime.plusSeconds(from.getStopDurationInSeconds());
            LocalDateTime entryTime = currentTime;

            int travelSeconds =
                    assignedTrain.getAdjustedTravelTime(conn.getBaseTravelTimeSeconds());
            LocalDateTime exitTime = entryTime.plusSeconds(travelSeconds);

            TrackOccupancy occupancy = new TrackOccupancy();
            occupancy.setConnection(conn);
            occupancy.setDirection(conn.getDirectionFrom(from));
            occupancy.setTrain(assignedTrain);
            occupancy.setTrip(this);
            occupancy.setEntryTime(entryTime);
            occupancy.setExitTime(exitTime);

            occupancies.add(occupancy);
            currentTime = exitTime;
        }

        return occupancies;
    }


}
