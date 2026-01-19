package com.example.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.CountableValueRange;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeFactory;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainSchedule {

    // --- PROBLEM FACTS (Immutable inputs) ---

    @ProblemFactCollectionProperty
    private List<Station> stations;

    @ProblemFactCollectionProperty
    private List<Connection> connections;

    @ProblemFactCollectionProperty
    private List<Route> routes;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "trainRange")
    private List<Train> trains;

    @ProblemFactCollectionProperty
    private List<StationDemand> stationDemands;

    // --- PLANNING ENTITIES ---

    @PlanningEntityCollectionProperty
    private List<ScheduledTrip> scheduledTrips;

    // --- SCORE ---

    @PlanningScore
    private HardMediumSoftScore score;

    // --- METADATA ---

    private LocalDate scheduleDate;
    private String scheduleName;
    private SolverStatus solverStatus;

    // --- VALUE RANGE PROVIDERS ---

    /**
     * Provides the range of possible departure times.
     * Defaults to 5:00 AM to 11:59 PM with 1-minute granularity.
     */
    @ValueRangeProvider(id = "departureTimeRange")
    public CountableValueRange<LocalDateTime> getDepartureTimeRange() {
        LocalDate date = scheduleDate != null ? scheduleDate : LocalDate.now();
        return ValueRangeFactory.createTemporalValueRange(
                date.atTime(5, 0),      // Start at 05:00
                date.atTime(23, 59),    // End at 23:59
                1,                       // Increment amount
                ChronoUnit.MINUTES       // 1-minute granularity
        );
    }

    // --- UTILITY METHODS ---

    /**
     * Get all scheduled trips for a specific train.
     */
    public List<ScheduledTrip> getTripsForTrain(Train train) {
        if (scheduledTrips == null || train == null) {
            return List.of();
        }
        return scheduledTrips.stream()
                .filter(trip -> train.equals(trip.getAssignedTrain()))
                .toList();
    }

    /**
     * Get all scheduled trips for a specific route.
     */
    public List<ScheduledTrip> getTripsForRoute(Route route) {
        if (scheduledTrips == null || route == null) {
            return List.of();
        }
        return scheduledTrips.stream()
                .filter(trip -> route.equals(trip.getRoute()))
                .toList();
    }

    /**
     * Get all track occupancies across all scheduled trips.
     */
    public List<TrackOccupancy> getAllTrackOccupancies() {
        if (scheduledTrips == null) {
            return List.of();
        }
        return scheduledTrips.stream()
                .filter(trip -> trip.getTrackOccupancies() != null)
                .flatMap(trip -> trip.getTrackOccupancies().stream())
                .toList();
    }

    /**
     * Get all station visits across all scheduled trips.
     */
    public List<StationVisit> getAllStationVisits() {
        if (scheduledTrips == null) {
            return List.of();
        }
        return scheduledTrips.stream()
                .filter(trip -> trip.getStationVisits() != null)
                .flatMap(trip -> trip.getStationVisits().stream())
                .toList();
    }

    /**
     * Get all depot stations.
     */
    public List<Station> getDepots() {
        if (stations == null) {
            return List.of();
        }
        return stations.stream()
                .filter(Station::isDepot)
                .toList();
    }
}
