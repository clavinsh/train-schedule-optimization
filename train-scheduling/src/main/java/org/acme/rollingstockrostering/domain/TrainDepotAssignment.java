package org.acme.rollingstockrostering.domain;

import java.util.Objects;

/**
 * TrainDepotAssignment - Problem Fact
 *
 * Represents the assignment of a train to its home depot.
 * Each train has exactly one home depot where it must start and end its daily operation.
 *
 * This is a problem fact (not a planning entity) - the train-depot assignments
 * are fixed input data, not something the solver optimizes.
 */
public class TrainDepotAssignment {

    private Long id;
    private Long vilciensId;    // Train ID
    private Long depoId;        // Home depot ID

    public TrainDepotAssignment() {
    }

    public TrainDepotAssignment(Long id, Long vilciensId, Long depoId) {
        this.id = id;
        this.vilciensId = vilciensId;
        this.depoId = depoId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVilciensId() {
        return vilciensId;
    }

    public void setVilciensId(Long vilciensId) {
        this.vilciensId = vilciensId;
    }

    public Long getDepoId() {
        return depoId;
    }

    public void setDepoId(Long depoId) {
        this.depoId = depoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainDepotAssignment that = (TrainDepotAssignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TrainDepotAssignment{" +
                "id=" + id +
                ", vilciensId=" + vilciensId +
                ", depoId=" + depoId +
                '}';
    }
}
