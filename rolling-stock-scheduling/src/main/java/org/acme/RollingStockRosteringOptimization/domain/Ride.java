package org.acme.RollingStockRosteringOptimization.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A ride represents a train journey from one station to another within a specific time interval.
 * This is the planning entity - the solver assigns trains to rides.
 * The duration depends on the distance between stations and the train speed.
 */
@PlanningEntity
@JsonIdentityInfo(scope = Ride.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Ride implements Comparable<Ride> {

    @PlanningId
    private String id;

    // The route this ride belongs to
    private Route route;

    // Starting station for this ride
    private Station departureStation;

    // Destination station for this ride
    private Station arrivalStation;

    // Departure time
    private LocalDateTime departureTime;

    // Arrival time (calculated based on distance and speed)
    private LocalDateTime arrivalTime;

    // The train assigned to this ride - this is the planning variable
    @PlanningVariable
    private Train train;

    public Ride() {
    }

    public Ride(String id) {
        this.id = id;
    }

    public Ride(String id, Route route, Station departureStation, Station arrivalStation) {
        this.id = id;
        this.route = route;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
    }

    public Ride(String id, Route route, Station departureStation, Station arrivalStation,
                LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.id = id;
        this.route = route;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Calculate the distance of this ride in kilometers.
     */
    @JsonIgnore
    public Double getDistanceKm() {
        if (departureStation == null || arrivalStation == null) {
            return null;
        }
        return departureStation.getDistanceTo(arrivalStation);
    }

    /**
     * Check if this ride overlaps in time with another ride.
     */
    @JsonIgnore
    public boolean overlaps(Ride other) {
        if (departureTime == null || arrivalTime == null ||
            other.departureTime == null || other.arrivalTime == null) {
            return false;
        }
        return !arrivalTime.isBefore(other.departureTime) &&
               !other.arrivalTime.isBefore(departureTime);
    }

    /**
     * Check if this ride covers a valid consecutive segment in its route.
     */
    @JsonIgnore
    public boolean isValidRouteSegment() {
        return route != null && route.isValidSegment(departureStation, arrivalStation);
    }

    /**
     * Get the segment index this ride covers in its route.
     * Returns -1 if not a valid segment.
     */
    @JsonIgnore
    public int getRouteSegmentIndex() {
        return route != null ? route.getSegmentIndex(departureStation, arrivalStation) : -1;
    }

    /**
     * Get a unique key for grouping rides by route and segment.
     * Format: "routeId:segmentIndex"
     */
    @JsonIgnore
    public String getRouteSegmentKey() {
        if (route == null) {
            return null;
        }
        int segmentIndex = getRouteSegmentIndex();
        return segmentIndex >= 0 ? route.getId() + ":" + segmentIndex : null;
    }

    @Override
    public String toString() {
        return id + "(" + departureStation + "->" + arrivalStation + ")";
    }

    @Override
    public int compareTo(Ride other) {
        if (departureTime == null && other.departureTime == null) {
            return id.compareTo(other.id);
        }
        if (departureTime == null) {
            return 1;
        }
        if (other.departureTime == null) {
            return -1;
        }
        int timeCompare = departureTime.compareTo(other.departureTime);
        return timeCompare != 0 ? timeCompare : id.compareTo(other.id);
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Station getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
    }

    public Station getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(Station arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Ride ride))
            return false;
        return Objects.equals(getId(), ride.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
