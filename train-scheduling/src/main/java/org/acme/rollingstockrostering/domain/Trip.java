package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Trip - PLANNING ENTITY
 *
 * Represents a complete train trip along a route, starting at a specific time.
 * This is the core planning entity that Timefold will optimize by assigning trains.
 *
 * A Trip covers an entire route from first station to last station.
 * The train assigned to a trip will visit ALL stations on the route.
 *
 * TIMEFOLD ANNOTATIONS:
 * @PlanningEntity - Identifies this as an entity to be optimized
 * @PlanningId - Unique identifier for this entity
 * @PlanningVariable - The field that Timefold will assign values to (vilciens)
 */
@PlanningEntity
public class Trip {

    @PlanningId
    private Long id;

    private Long marsrutaId;        // Route ID - defines which stations are visited
    private LocalTime startTime;    // Departure time from the FIRST station
    private boolean isReturnTrip;   // true if this is a return trip (route traversed in reverse)

    /**
     * PLANNING VARIABLE - This is what Timefold optimizes!
     *
     * Timefold will assign a train from the "vilcienuRange" to each trip.
     * The assigned train will serve ALL stations on the route.
     */
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange")
    private Vilciens vilciens;

    // No-arg constructor required by Timefold
    public Trip() {
    }

    public Trip(Long id, Long marsrutaId, LocalTime startTime, boolean isReturnTrip) {
        this.id = id;
        this.marsrutaId = marsrutaId;
        this.startTime = startTime;
        this.isReturnTrip = isReturnTrip;
    }

    /**
     * Calculate arrival time at a specific station index on the route.
     *
     * @param stationIndex 0-based index of the station on the route
     * @param travelTimePerStationMinutes time to travel between adjacent stations
     * @param stopTimeMinutes time spent at each station
     * @return arrival time at the station
     */
    public LocalTime getArrivalTimeAtStation(int stationIndex, long travelTimePerStationMinutes,
                                              long stopTimeMinutes) {
        if (stationIndex == 0) {
            return startTime;
        }
        // Travel time to reach this station + stop times at previous stations
        long totalMinutes = stationIndex * travelTimePerStationMinutes +
                           (stationIndex - 1) * stopTimeMinutes;
        return startTime.plusMinutes(totalMinutes);
    }

    /**
     * Calculate departure time from a specific station index on the route.
     *
     * @param stationIndex 0-based index of the station on the route
     * @param travelTimePerStationMinutes time to travel between adjacent stations
     * @param stopTimeMinutes time spent at each station
     * @return departure time from the station
     */
    public LocalTime getDepartureTimeFromStation(int stationIndex, long travelTimePerStationMinutes,
                                                  long stopTimeMinutes) {
        LocalTime arrival = getArrivalTimeAtStation(stationIndex, travelTimePerStationMinutes, stopTimeMinutes);
        return arrival.plusMinutes(stopTimeMinutes);
    }

    /**
     * Get the end time of this trip (arrival at last station).
     *
     * @param routeLength number of stations on the route
     * @param travelTimePerStationMinutes time to travel between adjacent stations
     * @param stopTimeMinutes time spent at each station
     * @return end time of the trip
     */
    public LocalTime getEndTime(int routeLength, long travelTimePerStationMinutes, long stopTimeMinutes) {
        return getArrivalTimeAtStation(routeLength - 1, travelTimePerStationMinutes, stopTimeMinutes);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarsrutaId() {
        return marsrutaId;
    }

    public void setMarsrutaId(Long marsrutaId) {
        this.marsrutaId = marsrutaId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public boolean isReturnTrip() {
        return isReturnTrip;
    }

    public void setReturnTrip(boolean returnTrip) {
        isReturnTrip = returnTrip;
    }

    public Vilciens getVilciens() {
        return vilciens;
    }

    public void setVilciens(Vilciens vilciens) {
        this.vilciens = vilciens;
    }

    public Long getVilciensId() {
        return vilciens == null ? null : vilciens.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", marsrutaId=" + marsrutaId +
                ", startTime=" + startTime +
                ", isReturnTrip=" + isReturnTrip +
                ", vilciensId=" + getVilciensId() +
                '}';
    }
}
