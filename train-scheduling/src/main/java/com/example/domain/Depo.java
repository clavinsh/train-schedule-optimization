package com.example.domain;

import java.util.Objects;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents a train depo with max train storage capacity at a specific station
 */
public class Depo {

    @PlanningId
    private Long id;
    private Station station;
    private int capacity;

    // No-arg constructor required by Jackson
    public Depo() {
    }

    public Depo(Long id, Station station, int capacity) {
        this.id = id;
        this.station = station;
        this.capacity = capacity;
    }

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
