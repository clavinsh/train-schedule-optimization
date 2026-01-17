package org.acme.RollingStockRosteringOptimization.domain;

import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Station.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Station implements Comparable<Station> {

    @PlanningId
    private String id;
    private String name;

    // Map of neighboring stations to distance in kilometers
    private Map<Station, Double> neighborDistances;

    public Station() {
    }

    public Station(String id) {
        this.id = id;
    }

    public Station(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Station(String id, String name, Map<Station, Double> neighborDistances) {
        this.id = id;
        this.name = name;
        this.neighborDistances = neighborDistances;
    }

    /**
     * Calculate travel time to a neighboring station in minutes.
     * Uses the constant train speed of 80 km/h from Configuration.
     */
    public double getTravelTimeInMinutes(Station destination, double trainSpeedKmh) {
        Double distanceKm = neighborDistances.get(destination);
        if (distanceKm == null) {
            return Double.MAX_VALUE; // Not a neighbor
        }
        return (distanceKm / trainSpeedKmh) * 60;
    }

    /**
     * Get distance to a neighboring station in kilometers.
     */
    public Double getDistanceTo(Station destination) {
        return neighborDistances != null ? neighborDistances.get(destination) : null;
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

    public Map<Station, Double> getNeighborDistances() {
        return neighborDistances;
    }

    public void setNeighborDistances(Map<Station, Double> neighborDistances) {
        this.neighborDistances = neighborDistances;
    }

    @Override
    public int compareTo(Station o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Station station))
            return false;
        return Objects.equals(getId(), station.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
