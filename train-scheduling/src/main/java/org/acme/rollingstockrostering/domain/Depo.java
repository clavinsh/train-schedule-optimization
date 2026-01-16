package org.acme.rollingstockrostering.domain;

import java.util.Objects;

/**
 * Depo (Depot) - Problem Fact
 *
 * Represents a physical depot location where trains can be stored and serviced. A depot is
 * associated with a station and can hold multiple trains. Trains must start their day from a depot
 * and return to a depot at end of day.
 */
public class Depo {

    private Long id;
    private String nosaukums; // Depot name
    private Long stacijaId; // Station ID where depot is located
    private int capacity; // Maximum number of trains the depot can hold

    public Depo() {}

    public Depo(Long id, String nosaukums, Long stacijaId, int capacity) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.stacijaId = stacijaId;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNosaukums() {
        return nosaukums;
    }

    public void setNosaukums(String nosaukums) {
        this.nosaukums = nosaukums;
    }

    public Long getStacijaId() {
        return stacijaId;
    }

    public void setStacijaId(Long stacijaId) {
        this.stacijaId = stacijaId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Backward compatibility - these methods are deprecated
    @Deprecated
    public Long getVilciensId() {
        return null; // No longer 1:1 with trains
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
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Depo{" + "id=" + id + ", nosaukums='" + nosaukums + '\'' + ", stacijaId="
                + stacijaId + ", capacity=" + capacity + '}';
    }
}
