package com.example.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class TrainDepoAssignment {
    @PlanningId
    private Long id;
    private Train train;
    private Depo depo;
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TrainDepoAssignment tda = (TrainDepoAssignment) o;
        return Objects.equals(id, tda.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
