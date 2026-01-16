package org.acme.rollingstockrostering.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Marsruts (Route) - Problem Fact
 *
 * Represents a train route with an ordered list of stations.
 * A route defines the sequence of stations a train must visit when assigned to a trip on this route.
 */
public class Marsruts {

    private Long id;
    private String nosaukums;       // Route name
    private List<Long> stacijas;    // Ordered list of station IDs (first to last)

    public Marsruts() {
        this.stacijas = new ArrayList<>();
    }

    public Marsruts(Long id, String nosaukums, List<Long> stacijas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.stacijas = stacijas != null ? stacijas : new ArrayList<>();
    }

    /**
     * Get the number of stations on this route.
     */
    public int getStationCount() {
        return stacijas != null ? stacijas.size() : 0;
    }

    /**
     * Get the first station ID on this route.
     */
    public Long getFirstStationId() {
        if (stacijas == null || stacijas.isEmpty()) {
            return null;
        }
        return stacijas.get(0);
    }

    /**
     * Get the last station ID on this route.
     */
    public Long getLastStationId() {
        if (stacijas == null || stacijas.isEmpty()) {
            return null;
        }
        return stacijas.get(stacijas.size() - 1);
    }

    /**
     * Get the station ID at a specific index.
     *
     * @param index 0-based index
     * @return station ID or null if index is invalid
     */
    public Long getStationIdAt(int index) {
        if (stacijas == null || index < 0 || index >= stacijas.size()) {
            return null;
        }
        return stacijas.get(index);
    }

    /**
     * Get the index of a station on this route.
     *
     * @param stationId the station ID to find
     * @return 0-based index, or -1 if not found
     */
    public int getStationIndex(Long stationId) {
        if (stacijas == null || stationId == null) {
            return -1;
        }
        return stacijas.indexOf(stationId);
    }

    /**
     * Check if this route contains a specific station.
     */
    public boolean containsStation(Long stationId) {
        return stacijas != null && stacijas.contains(stationId);
    }

    /**
     * Get the stations in reverse order (for return trips).
     */
    public List<Long> getStacijasReversed() {
        if (stacijas == null) {
            return new ArrayList<>();
        }
        List<Long> reversed = new ArrayList<>(stacijas);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Get stations for a trip (forward or reverse based on direction).
     *
     * @param isReturnTrip if true, returns reversed station list
     */
    public List<Long> getStacijasForTrip(boolean isReturnTrip) {
        return isReturnTrip ? getStacijasReversed() : new ArrayList<>(stacijas);
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

    public List<Long> getStacijas() {
        return stacijas;
    }

    public void setStacijas(List<Long> stacijas) {
        this.stacijas = stacijas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Marsruts marsruts = (Marsruts) o;
        return Objects.equals(id, marsruts.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Marsruts{" +
                "id=" + id +
                ", nosaukums='" + nosaukums + '\'' +
                ", stationCount=" + getStationCount() +
                '}';
    }
}
