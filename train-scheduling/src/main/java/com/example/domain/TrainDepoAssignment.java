package com.example.domain;

import java.util.Objects;

public class TrainDepoAssignment {

    private Long id;
    private Train train;
    private Depo depo;

    // No-arg constructor required by Jackson
    public TrainDepoAssignment() {
    }

    public TrainDepoAssignment(Long id, Train train, Depo depo) {
        this.id = id;
        this.train = train;
        this.depo = depo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Depo getDepo() {
        return depo;
    }

    public void setDepo(Depo depo) {
        this.depo = depo;
    }

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
