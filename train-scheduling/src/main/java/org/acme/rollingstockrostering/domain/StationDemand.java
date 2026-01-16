package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;
import java.util.Objects;

/**
 * StationDemand - Problem Fact
 *
 * Represents passenger demand at a specific station on a specific route at a specific time.
 * Includes both boarding and alighting passengers to enable cumulative capacity tracking.
 *
 * The demand values represent:
 * - boardingPassengers: number of passengers wanting to board at this station
 * - alightingPassengers: number of passengers wanting to exit at this station
 *
 * These values are given per hour, and the actual demand should be linearly interpolated
 * based on the exact arrival time of a train.
 */
public class StationDemand {

    private Long id;
    private Long stacijasId;        // Station ID
    private Long marsrutaId;        // Route ID
    private int stationIndex;       // Position of station on the route (0-based)
    private LocalTime hour;         // Hour of demand (e.g., 08:00, 09:00)
    private int boardingPassengers; // Passengers wanting to board
    private int alightingPassengers; // Passengers wanting to alight

    public StationDemand() {
    }

    public StationDemand(Long id, Long stacijasId, Long marsrutaId, int stationIndex,
                         LocalTime hour, int boardingPassengers, int alightingPassengers) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.stationIndex = stationIndex;
        this.hour = hour;
        this.boardingPassengers = boardingPassengers;
        this.alightingPassengers = alightingPassengers;
    }

    /**
     * Calculate interpolated boarding demand based on actual arrival time.
     *
     * If demand is given per hour, and a train arrives at minute N of that hour,
     * the demand is linearly interpolated: demand * (60 - N) / 60
     *
     * This represents passengers who have accumulated waiting for the train.
     *
     * @param arrivalTime actual arrival time of the train
     * @return interpolated boarding demand
     */
    public int getInterpolatedBoardingDemand(LocalTime arrivalTime) {
        if (arrivalTime.getHour() != hour.getHour()) {
            return 0; // Different hour, no demand from this hour slot
        }
        int minutesIntoHour = arrivalTime.getMinute();
        // More minutes passed = more passengers accumulated (proportional)
        // At minute 0: 0 passengers accumulated yet
        // At minute 30: half the hourly demand accumulated
        // At minute 60: full hourly demand accumulated
        return (int) Math.round(boardingPassengers * (minutesIntoHour + 1) / 60.0);
    }

    /**
     * Calculate interpolated alighting demand based on actual arrival time.
     *
     * @param arrivalTime actual arrival time of the train
     * @return interpolated alighting demand
     */
    public int getInterpolatedAlightingDemand(LocalTime arrivalTime) {
        if (arrivalTime.getHour() != hour.getHour()) {
            return 0;
        }
        int minutesIntoHour = arrivalTime.getMinute();
        return (int) Math.round(alightingPassengers * (minutesIntoHour + 1) / 60.0);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStacijasId() {
        return stacijasId;
    }

    public void setStacijasId(Long stacijasId) {
        this.stacijasId = stacijasId;
    }

    public Long getMarsrutaId() {
        return marsrutaId;
    }

    public void setMarsrutaId(Long marsrutaId) {
        this.marsrutaId = marsrutaId;
    }

    public int getStationIndex() {
        return stationIndex;
    }

    public void setStationIndex(int stationIndex) {
        this.stationIndex = stationIndex;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }

    public int getBoardingPassengers() {
        return boardingPassengers;
    }

    public void setBoardingPassengers(int boardingPassengers) {
        this.boardingPassengers = boardingPassengers;
    }

    public int getAlightingPassengers() {
        return alightingPassengers;
    }

    public void setAlightingPassengers(int alightingPassengers) {
        this.alightingPassengers = alightingPassengers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationDemand that = (StationDemand) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StationDemand{" +
                "id=" + id +
                ", stacijasId=" + stacijasId +
                ", marsrutaId=" + marsrutaId +
                ", stationIndex=" + stationIndex +
                ", hour=" + hour +
                ", boardingPassengers=" + boardingPassengers +
                ", alightingPassengers=" + alightingPassengers +
                '}';
    }
}
