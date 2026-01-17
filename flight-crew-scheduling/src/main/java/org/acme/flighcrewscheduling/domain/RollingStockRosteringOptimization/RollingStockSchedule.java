package org.acme.flighcrewscheduling.domain.RollingStockRosteringOptimization;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * The planning solution for rolling stock rostering optimization.
 * Contains all problem facts and planning entities.
 *
 * Hard constraints:
 * 1. Train must visit all stations in its assigned route
 * 2. Train cannot exceed passenger capacity
 * 3. Train must start and end the day at a depot
 *
 * Soft constraints:
 * 1. Maximize passengers onloaded
 * 2. Minimize total traveled distance
 */
@PlanningSolution
public class RollingStockSchedule {

    // Problem facts - immutable data
    @ProblemFactCollectionProperty
    private List<Station> stations;

    @ProblemFactCollectionProperty
    private List<Depo> depos;

    @ProblemFactCollectionProperty
    private List<Route> routes;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Train> trains;

    @ProblemFactCollectionProperty
    private List<Demand> demands;

    @ProblemFactCollectionProperty
    private List<TimeTable> timeTables;

    @ProblemFactProperty
    private Configuration configuration;

    // Planning entities - what gets optimized
    @PlanningEntityCollectionProperty
    private List<Ride> rides;

    // Score - optimization objective
    @PlanningScore
    private HardSoftLongScore score;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    public RollingStockSchedule() {
    }

    public RollingStockSchedule(HardSoftLongScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Depo> getDepos() {
        return depos;
    }

    public void setDepos(List<Depo> depos) {
        this.depos = depos;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Train> getTrains() {
        return trains;
    }

    public void setTrains(List<Train> trains) {
        this.trains = trains;
    }

    public List<Demand> getDemands() {
        return demands;
    }

    public void setDemands(List<Demand> demands) {
        this.demands = demands;
    }

    public List<TimeTable> getTimeTables() {
        return timeTables;
    }

    public void setTimeTables(List<TimeTable> timeTables) {
        this.timeTables = timeTables;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<Ride> getRides() {
        return rides;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
