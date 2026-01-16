package org.acme.rollingstockrostering.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stacija (Station) - Problem Fact
 *
 * Represents a railway station with its location and connections to other stations.
 * Stations are nodes in the railway network graph.
 */
public class Stacija {

    private Long id;
    private String nosaukums;               // Station name
    private GeoCoordinates koordinatas;     // Geographic coordinates
    private List<Long> kaiminiStacijas;     // IDs of neighboring stations (graph edges)

    public Stacija() {
        this.kaiminiStacijas = new ArrayList<>();
    }

    public Stacija(Long id, String nosaukums, GeoCoordinates koordinatas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.koordinatas = koordinatas;
        this.kaiminiStacijas = new ArrayList<>();
    }

    public Stacija(Long id, String nosaukums, GeoCoordinates koordinatas, List<Long> kaiminiStacijas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.koordinatas = koordinatas;
        this.kaiminiStacijas = kaiminiStacijas != null ? kaiminiStacijas : new ArrayList<>();
    }

    /**
     * Calculate distance to another station in kilometers using Haversine formula.
     *
     * @param other the other station
     * @return distance in kilometers, or 0.0 if coordinates are missing
     */
    public double distanceTo(Stacija other) {
        if (this.koordinatas == null || other == null || other.koordinatas == null) {
            return 0.0;
        }
        return this.koordinatas.distanceTo(other.koordinatas);
    }

    /**
     * Check if this station is directly connected to another station.
     *
     * @param otherStationId the ID of the other station
     * @return true if stations are neighbors
     */
    public boolean isConnectedTo(Long otherStationId) {
        return kaiminiStacijas != null && kaiminiStacijas.contains(otherStationId);
    }

    // Getters and setters
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

    public GeoCoordinates getKoordinatas() {
        return koordinatas;
    }

    public void setKoordinatas(GeoCoordinates koordinatas) {
        this.koordinatas = koordinatas;
    }

    public List<Long> getKaiminiStacijas() {
        return kaiminiStacijas;
    }

    public void setKaiminiStacijas(List<Long> kaiminiStacijas) {
        this.kaiminiStacijas = kaiminiStacijas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stacija stacija = (Stacija) o;
        return Objects.equals(id, stacija.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Stacija{" +
                "id=" + id +
                ", nosaukums='" + nosaukums + '\'' +
                ", koordinatas=" + koordinatas +
                '}';
    }
}
