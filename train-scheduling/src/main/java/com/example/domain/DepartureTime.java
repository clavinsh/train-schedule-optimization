package com.example.domain;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a train departure from a specific station on a route at a specific time.
 * This is the core planning entity that Timefold optimizes.
 *
 * Maps to "Atiešanas laiks" from the domain model.
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@PlanningEntity(comparatorClass = DepartureTime.DifficultyComparator.class)
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

    // Lookup map for station demands by hour (populated during data generation)
    private Map<Integer, StationDemand> hourlyDemands;

    // Planning variables - Timefold will optimize these
    @PlanningVariable(valueRangeProviderRefs = "trainRange")
    private Train train;

    @PlanningVariable(valueRangeProviderRefs = "timeRange")
    private LocalTime departureTime;

    // Shadow variable - automatically calculated from departureTime
    @ShadowVariable(supplierName = "calculatePassengerDelta")
    private Integer passengerDelta;

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
    @JsonIgnore
    public Station getNextStation() {
        if (isLastStation()) {
            return null;
        }
        return route.getStations().get(stationIndexInRoute + 1);
    }

    /**
     * Gets the previous station on the route, or null if this is the first station
     */
    @JsonIgnore
    public Station getPreviousStation() {
        if (isFirstStation()) {
            return null;
        }
        return route.getStations().get(stationIndexInRoute - 1);
    }

    /**
     * Supplier method for the passengerDelta shadow variable.
     * Calculates net passenger change (embarking - disembarking) at this stop.
     */
    @ShadowSources("departureTime")
    public Integer calculatePassengerDelta() {
        if (departureTime == null || hourlyDemands == null) {
            return 0;
        }
        StationDemand demand = hourlyDemands.get(departureTime.getHour());
        if (demand == null) {
            return 0;
        }
        return demand.getEmbarkingPassengers(departureTime) - demand.getDisembarkingPassengers(departureTime);
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
