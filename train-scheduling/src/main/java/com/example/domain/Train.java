package com.example.domain;

import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a train with a maximum passenger capacity
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Train {
    @PlanningId
    private Long id;
    private int capacity;
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Train train = (Train) o;
        return Objects.equals(id, train.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
