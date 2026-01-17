package org.acme.RollingStockRosteringOptimization.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A train that can carry passengers along routes.
 * Each train has a passenger capacity and a home depot where it starts and ends the day.
 */
@JsonIdentityInfo(scope = Train.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Train {

    @PlanningId
    private String id;

    // Maximum number of passengers this train can carry
    private int capacity;

    // The depot where this train starts and ends its day
    private Depo homeDepo;

    // Current station where the train is located
    private Station currentStation;

    // Total distance traveled by this train in kilometers
    private double traveledDistanceKm;

    public Train() {
    }

    public Train(String id) {
        this.id = id;
    }

    public Train(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public Train(String id, int capacity, Depo homeDepo) {
        this.id = id;
        this.capacity = capacity;
        this.homeDepo = homeDepo;
    }

    /**
     * Check if this train can accommodate the given number of passengers.
     */
    public boolean canAccommodate(int passengerCount) {
        return passengerCount <= capacity;
    }

    /**
     * Add distance to the total traveled distance.
     */
    public void addTraveledDistance(double distanceKm) {
        this.traveledDistanceKm += distanceKm;
    }

    @Override
    public String toString() {
        return "Train-" + id;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Depo getHomeDepo() {
        return homeDepo;
    }

    public void setHomeDepo(Depo homeDepo) {
        this.homeDepo = homeDepo;
    }

    public Station getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(Station currentStation) {
        this.currentStation = currentStation;
    }

    public double getTraveledDistanceKm() {
        return traveledDistanceKm;
    }

    public void setTraveledDistanceKm(double traveledDistanceKm) {
        this.traveledDistanceKm = traveledDistanceKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Train train))
            return false;
        return Objects.equals(getId(), train.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
