package org.acme.RollingStockRosteringOptimization.domain;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A train route consisting of an ordered list of stations.
 * The train can only travel in the direction from starting station to destination station.
 * If the train wants to repeat the route, it must return empty to the starting station first.
 */
@JsonIdentityInfo(scope = Route.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Route {

    @PlanningId
    private String id;
    private String name;

    // Ordered list of stations from start to destination
    private List<Station> stations;

    public Route() {
    }

    public Route(String id) {
        this.id = id;
    }

    public Route(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Route(String id, String name, List<Station> stations) {
        this.id = id;
        this.name = name;
        this.stations = stations;
    }

    @JsonIgnore
    public Station getStartStation() {
        return stations != null && !stations.isEmpty() ? stations.get(0) : null;
    }

    @JsonIgnore
    public Station getDestinationStation() {
        return stations != null && !stations.isEmpty() ? stations.get(stations.size() - 1) : null;
    }

    /**
     * Calculate the total distance of this route in kilometers.
     */
    @JsonIgnore
    public double getTotalDistanceKm() {
        if (stations == null || stations.size() < 2) {
            return 0;
        }
        double totalDistance = 0;
        for (int i = 0; i < stations.size() - 1; i++) {
            Station from = stations.get(i);
            Station to = stations.get(i + 1);
            Double distance = from.getDistanceTo(to);
            if (distance != null) {
                totalDistance += distance;
            }
        }
        return totalDistance;
    }

    /**
     * Check if a station is part of this route.
     */
    public boolean containsStation(Station station) {
        return stations != null && stations.contains(station);
    }

    /**
     * Get the index of a station in this route.
     */
    public int getStationIndex(Station station) {
        return stations != null ? stations.indexOf(station) : -1;
    }

    /**
     * Get the number of segments in this route.
     * A segment is a consecutive pair of stations.
     */
    @JsonIgnore
    public int getSegmentCount() {
        return stations != null && stations.size() > 1 ? stations.size() - 1 : 0;
    }

    /**
     * Check if the given departure and arrival stations form a valid consecutive segment in this route.
     */
    public boolean isValidSegment(Station departure, Station arrival) {
        if (stations == null || stations.size() < 2 || departure == null || arrival == null) {
            return false;
        }
        int departureIndex = stations.indexOf(departure);
        int arrivalIndex = stations.indexOf(arrival);
        return departureIndex >= 0 && arrivalIndex == departureIndex + 1;
    }

    /**
     * Get the segment index for a departure/arrival pair.
     * Returns -1 if not a valid segment.
     */
    public int getSegmentIndex(Station departure, Station arrival) {
        if (!isValidSegment(departure, arrival)) {
            return -1;
        }
        return stations.indexOf(departure);
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Route route))
            return false;
        return Objects.equals(getId(), route.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
