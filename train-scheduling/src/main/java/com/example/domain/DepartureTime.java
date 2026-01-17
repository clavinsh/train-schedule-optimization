package com.example.domain;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.Objects;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Represents a train departure from a specific station on a route at a specific time.
 * This is the core planning entity that Timefold optimizes.
 *
 * Maps to "Atiešanas laiks" from the domain model.
 */
@PlanningEntity(difficultyComparatorClass = DepartureTime.DifficultyComparator.class)
public class DepartureTime {

    /**
     * Difficulty comparator for construction heuristic.
     * Longer routes are harder to schedule, so they should be assigned first.
     */
    public static class DifficultyComparator implements Comparator<DepartureTime> {
        @Override
        public int compare(DepartureTime a, DepartureTime b) {
            // Compare by route length (number of stations) - longer routes are harder
            int aStations = a.getRoute() != null ? a.getRoute().getStations().size() : 0;
            int bStations = b.getRoute() != null ? b.getRoute().getStations().size() : 0;
            return Integer.compare(aStations, bStations);
        }
    }

    @PlanningId
    private Long id;

    // Problem facts - fixed input
    private Station station;
    private Route route;
    private int stationIndexInRoute; // Position of this station in the route

    // Planning variables - Timefold will optimize these
    @PlanningVariable(valueRangeProviderRefs = "trainRange")
    private Train train;

    @PlanningVariable(valueRangeProviderRefs = "timeRange")
    private LocalTime departureTime;

    // Calculated during solving - tracks passenger changes at this stop
    private int passengerDelta;

    // No-arg constructor required by Timefold
    public DepartureTime() {
    }

    public DepartureTime(Long id, Station station, Route route, int stationIndexInRoute) {
        this.id = id;
        this.station = station;
        this.route = route;
        this.stationIndexInRoute = stationIndexInRoute;
        this.passengerDelta = 0;
    }

    /**
     * Checks if this departure is the first station on the route
     */
    public boolean isFirstStation() {
        return stationIndexInRoute == 0;
    }

    /**
     * Checks if this departure is the last station on the route
     */
    public boolean isLastStation() {
        return stationIndexInRoute == route.getStations().size() - 1;
    }

    /**
     * Gets the next station on the route, or null if this is the last station
     */
    public Station getNextStation() {
        if (isLastStation()) {
            return null;
        }
        return route.getStations().get(stationIndexInRoute + 1);
    }

    /**
     * Gets the previous station on the route, or null if this is the first station
     */
    public Station getPreviousStation() {
        if (isFirstStation()) {
            return null;
        }
        return route.getStations().get(stationIndexInRoute - 1);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public int getStationIndexInRoute() {
        return stationIndexInRoute;
    }

    public void setStationIndexInRoute(int stationIndexInRoute) {
        this.stationIndexInRoute = stationIndexInRoute;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getPassengerDelta() {
        return passengerDelta;
    }

    public void setPassengerDelta(int passengerDelta) {
        this.passengerDelta = passengerDelta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DepartureTime that = (DepartureTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DepartureTime{" +
                "station=" + (station != null ? station.getName() : "null") +
                ", route=" + (route != null ? route.getName() : "null") +
                ", train=" + (train != null ? train.getId() : "null") +
                ", time=" + departureTime +
                '}';
    }
}
