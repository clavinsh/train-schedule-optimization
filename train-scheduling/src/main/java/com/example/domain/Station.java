package com.example.domain;

import java.util.List;
import java.util.Objects;
import com.example.domain.helpers.CoordinateCalc;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a railway station with its location and direct connections to other neighboring
 * stations.
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Station implements Comparable<Station> {
    @PlanningId
    private Long id;
    private String name;
    private double latitude;
    private double longitude;

    @JsonIgnore // Prevent circular reference during JSON serialization
    private List<Station> neighbors;


    @Override
    public int compareTo(Station arg0) {
        return id.compareTo(arg0.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public double distanceTo(Station other) {
        return CoordinateCalc.haversineDistance(this.latitude, this.longitude, other.latitude,
                other.longitude);
    }
}
