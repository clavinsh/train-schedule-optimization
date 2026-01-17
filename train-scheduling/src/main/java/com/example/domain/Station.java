package com.example.domain;

import java.util.List;
import java.util.Objects;
import com.example.domain.helpers.CoordinateCalc;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents a railway station with its location and direct connections to other neighboring
 * stations.
 */
public class Station implements Comparable<Station> {

    @PlanningId
    private Long id;
    private String name;
    private double latitude;
    private double longitude;

    @JsonIgnore // Prevent circular reference during JSON serialization
    private List<Station> neighbors;

    // No-arg constructor required by Jackson
    public Station() {
    }

    public Station(Long id, String name, double latitude, double longitude,
            List<Station> neighbors) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighbors = neighbors;
    }

    @Override
    public int compareTo(Station arg0) {
        return id.compareTo(arg0.id);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String nosaukums) {
        this.name = nosaukums;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public List<Station> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Station> neighbors) {
        this.neighbors = neighbors;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public double distanceTo(Station other) {
        return CoordinateCalc.haversineDistance(this.latitude, this.longitude, other.latitude,
                other.longitude);
    }
}
