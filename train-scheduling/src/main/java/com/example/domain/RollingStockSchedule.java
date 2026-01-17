package com.example.domain;

import java.time.LocalTime;
import java.util.List;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 * The planning solution for rolling stock scheduling optimization.
 * Contains all problem facts (input data) and planning entities (what Timefold optimizes).
 */
@PlanningSolution
public class RollingStockSchedule {

    // Problem facts - input data that doesn't change during solving

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "trainRange")
    private List<Train> trains;

    @ProblemFactCollectionProperty
    private List<Station> stations;

    @ProblemFactCollectionProperty
    private List<Route> routes;

    @ProblemFactCollectionProperty
    private List<Depo> depos;

    @ProblemFactCollectionProperty
    private List<TrainDepoAssignment> trainDepoAssignments;

    @ProblemFactCollectionProperty
    private List<StationDemand> stationDemands;

    @ProblemFactProperty
    private TrainConfiguration configuration;

    // Value range for departure times (generated list of possible times)
    @ValueRangeProvider(id = "timeRange")
    @ProblemFactCollectionProperty
    private List<LocalTime> availableDepartureTimes;

    // Planning entities - what Timefold optimizes
    @PlanningEntityCollectionProperty
    private List<DepartureTime> departureTimes;

    // The score calculated by the constraint provider
    @PlanningScore
    private HardSoftScore score;

    // No-arg constructor required by Timefold
    public RollingStockSchedule() {
    }

    public RollingStockSchedule(List<Train> trains, List<Station> stations, List<Route> routes,
            List<Depo> depos, List<TrainDepoAssignment> trainDepoAssignments,
            List<StationDemand> stationDemands, TrainConfiguration configuration,
            List<LocalTime> availableDepartureTimes, List<DepartureTime> departureTimes) {
        this.trains = trains;
        this.stations = stations;
        this.routes = routes;
        this.depos = depos;
        this.trainDepoAssignments = trainDepoAssignments;
        this.stationDemands = stationDemands;
        this.configuration = configuration;
        this.availableDepartureTimes = availableDepartureTimes;
        this.departureTimes = departureTimes;
    }

    // Getters and setters

    public List<Train> getTrains() {
        return trains;
    }

    public void setTrains(List<Train> trains) {
        this.trains = trains;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Depo> getDepos() {
        return depos;
    }

    public void setDepos(List<Depo> depos) {
        this.depos = depos;
    }

    public List<TrainDepoAssignment> getTrainDepoAssignments() {
        return trainDepoAssignments;
    }

    public void setTrainDepoAssignments(List<TrainDepoAssignment> trainDepoAssignments) {
        this.trainDepoAssignments = trainDepoAssignments;
    }

    public List<StationDemand> getStationDemands() {
        return stationDemands;
    }

    public void setStationDemands(List<StationDemand> stationDemands) {
        this.stationDemands = stationDemands;
    }

    public TrainConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TrainConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<LocalTime> getAvailableDepartureTimes() {
        return availableDepartureTimes;
    }

    public void setAvailableDepartureTimes(List<LocalTime> availableDepartureTimes) {
        this.availableDepartureTimes = availableDepartureTimes;
    }

    public List<DepartureTime> getDepartureTimes() {
        return departureTimes;
    }

    public void setDepartureTimes(List<DepartureTime> departureTimes) {
        this.departureTimes = departureTimes;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
