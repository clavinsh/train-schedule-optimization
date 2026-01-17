package org.acme.RollingStockRosteringOptimization.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // @ProblemFactCollectionProperty
    // private List<TimeTable> timeTables;

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

    // public List<TimeTable> getTimeTables() {
    //     return timeTables;
    // }

    // public void setTimeTables(List<TimeTable> timeTables) {
    //     this.timeTables = timeTables;
    // }

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

    /**
     * Calculate and update the currentPassengerCount for all trains based on their assigned rides.
     * This should be called after solving to populate passenger counts for display.
     *
     * For each train, we:
     * 1. Find all rides assigned to this train
     * 2. Sort them by departure time
     * 3. For each ride, board passengers based on demand at the departure station (up to remaining capacity)
     */
    public void calculatePassengerCounts() {
        if (trains == null || rides == null || demands == null) {
            return;
        }

        // Create a map of station -> demand for quick lookup
        Map<Station, Demand> demandByStation = demands.stream()
                .collect(Collectors.toMap(Demand::getStation, d -> d, (a, b) -> a));

        // Reset all train passenger counts
        for (Train train : trains) {
            train.setCurrentPassengerCount(0);
        }

        // Group rides by train
        Map<Train, List<Ride>> ridesByTrain = rides.stream()
                .filter(ride -> ride.getTrain() != null)
                .collect(Collectors.groupingBy(Ride::getTrain));

        // For each train, process rides in chronological order
        for (Map.Entry<Train, List<Ride>> entry : ridesByTrain.entrySet()) {
            Train train = entry.getKey();
            List<Ride> trainRides = entry.getValue();

            // Sort rides by departure time
            trainRides.sort(Comparator.comparing(Ride::getDepartureTime));

            // Process each ride and accumulate passengers
            for (Ride ride : trainRides) {
                Station departureStation = ride.getDepartureStation();
                Demand demand = demandByStation.get(departureStation);

                if (demand != null && ride.getDepartureTime() != null) {
                    int hour = ride.getDepartureTime().getHour();
                    int waitingPassengers = demand.getDemandAtHour(hour);
                    // Board as many passengers as possible (up to remaining capacity)
                    train.boardPassengers(waitingPassengers);
                }
            }
        }
    }
}
