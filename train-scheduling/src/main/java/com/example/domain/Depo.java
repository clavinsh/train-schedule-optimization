package com.example.domain;

import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a train depo with max train storage capacity at a specific station
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Depo {
    @PlanningId
    private Long id;
    private Station station;
    private int capacity;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Depo depo = (Depo) o;
        return Objects.equals(id, depo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return station.getName() + " - Depo";
    }
}
