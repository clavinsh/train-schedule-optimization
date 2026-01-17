package org.acme.flighcrewscheduling.domain.RollingStockRosteringOptimization;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A depot where trains start and end their day.
 * Trains must return to a depot at the end of operations.
 */
@JsonIdentityInfo(scope = Depo.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Depo {

    @PlanningId
    private String id;
    private Station station;

    public Depo() {
    }

    public Depo(String id) {
        this.id = id;
    }

    public Depo(String id, Station station) {
        this.id = id;
        this.station = station;
    }

    @Override
    public String toString() {
        return "Depo-" + id + "@" + (station != null ? station.getName() : "?");
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

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Depo depo))
            return false;
        return Objects.equals(getId(), depo.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
