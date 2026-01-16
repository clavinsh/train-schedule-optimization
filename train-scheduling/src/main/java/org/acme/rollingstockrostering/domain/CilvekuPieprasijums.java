package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;
import java.util.Objects;

/**
 * CilvēkuPieprasījums (PassengerDemand) - Problem Fact
 * 
 * Represents passenger demand at a specific station, route, and time. Used to calculate how many
 * passengers want to board at each station.
 *
 * The demand is given per hour. When a train arrives at minute N of that hour, the actual demand
 * should be linearly interpolated based on wait time.
 */
public class CilvekuPieprasijums {

    private Long id;
    private Long stacijasId; // Station ID
    private Long marsrutaId; // Route ID
    private LocalTime stunda; // Hour of demand
    private int cilvekuSkaits; // Number of passengers

    public CilvekuPieprasijums() {}

    public CilvekuPieprasijums(Long id, Long stacijasId, Long marsrutaId, LocalTime stunda,
            int cilvekuSkaits) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.stunda = stunda;
        this.cilvekuSkaits = cilvekuSkaits;
    }

    /**
     * Calculate interpolated demand based on actual arrival time.
     *
     * Since demand is given per hour, and a train can arrive at minute N, passengers have been
     * accumulating linearly based on the since the hour started
     *
     * @param arrivalTime the actual arrival time of the train
     * @return interpolated passenger demand
     */
    public int getInterpolatedDemand(LocalTime arrivalTime) {
        if (arrivalTime == null || stunda == null) {
            return 0;
        }

        if (arrivalTime.getHour() != stunda.getHour()) {
            return 0;
        }

        int minutesIntoHour = arrivalTime.getMinute();
        return (int) Math.round(cilvekuSkaits * (minutesIntoHour + 1) / 60.0);
    }

    /**
     * Check if this demand applies to a specific arrival time. Demand for hour H applies to
     * arrivals during hour H.
     */
    public boolean appliesTo(LocalTime arrivalTime) {
        return arrivalTime != null && stunda != null && arrivalTime.getHour() == stunda.getHour();
    }

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

    public LocalTime getStunda() {
        return stunda;
    }

    public void setStunda(LocalTime stunda) {
        this.stunda = stunda;
    }

    public int getCilvekuSkaits() {
        return cilvekuSkaits;
    }

    public void setCilvekuSkaits(int cilvekuSkaits) {
        this.cilvekuSkaits = cilvekuSkaits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CilvekuPieprasijums that = (CilvekuPieprasijums) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CilvekuPieprasijums{" + "id=" + id + ", stacijasId=" + stacijasId + ", marsrutaId="
                + marsrutaId + ", stunda=" + stunda + ", cilvekuSkaits=" + cilvekuSkaits + '}';
    }
}
