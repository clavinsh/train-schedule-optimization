package com.example.domain;

import java.util.List;
import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents a specific train route covering some ordererd list of stations
 */
public class Route implements Comparable<Route> {

    @PlanningId
    private Long id;
    private String name;
    private List<Station> stations;

    // No-arg constructor required by Jackson
    public Route() {
    }

    public Route(Long id, String name, List<Station> stations) {
        this.id = id;
        this.name = name;
        this.stations = stations;
    }

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

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }


    @Override
    public int compareTo(Route arg0) {
        return id.compareTo(arg0.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Route route = (Route) o;
        return Objects.equals(id, route.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
