package com.example.domain;

import java.time.LocalTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a train with a maximum passenger capacity.
 * Acts as an anchor for the chained Trip variable.
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Train implements Standstill {
    private Long id;
    private int capacity;
    private Depo homeDepo;
    private LocalTime departureTime;

    @JsonIgnore
    private Trip nextTrip;

    @Override
    public Station getStation() {
        return homeDepo != null ? homeDepo.getStation() : null;
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
        return Objects.hash(id);
    }
}
