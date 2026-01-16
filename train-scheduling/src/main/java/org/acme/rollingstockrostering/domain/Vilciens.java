package org.acme.rollingstockrostering.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Vilciens (Train) - Problem Fact
 *
 * Represents a train with its passenger capacity.
 * This is a problem fact that provides the value range for the planning variable.
 * The solver will assign trains to trips.
 */
public class Vilciens {

    @PlanningId
    private Long id;
    private int kapacitate;     // Passenger capacity
    private String nosaukums;   // Train name/identifier (optional)

    // No-arg constructor required by Timefold
    public Vilciens() {
    }

    public Vilciens(Long id, int kapacitate) {
        this.id = id;
        this.kapacitate = kapacitate;
        this.nosaukums = "Train " + id;
    }

    public Vilciens(Long id, int kapacitate, String nosaukums) {
        this.id = id;
        this.kapacitate = kapacitate;
        this.nosaukums = nosaukums;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getKapacitate() {
        return kapacitate;
    }

    public void setKapacitate(int kapacitate) {
        this.kapacitate = kapacitate;
    }

    public String getNosaukums() {
        return nosaukums;
    }

    public void setNosaukums(String nosaukums) {
        this.nosaukums = nosaukums;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vilciens vilciens = (Vilciens) o;
        return Objects.equals(id, vilciens.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vilciens{" +
                "id=" + id +
                ", kapacitate=" + kapacitate +
                ", nosaukums='" + nosaukums + '\'' +
                '}';
    }
}
