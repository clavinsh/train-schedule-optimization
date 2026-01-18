package com.example.domain;

import java.util.List;
import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a specific train route covering some ordered list of stations.
 * This is a template of stations, not a specific trip instance.
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Route implements Comparable<Route> {
    @PlanningId
    private Long id;
    private String name;
    private List<Station> stations;

    @Override
    public int compareTo(Route other) {
        return id.compareTo(other.id);
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
