package com.example.domain;

import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents a train with a maximum passenger capacity
 */
public class Train {

    @PlanningId
    private Long id;
    private int capacity;

    // No-arg constructor required by Jackson
    public Train() {
    }

    public Train(Long id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

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
