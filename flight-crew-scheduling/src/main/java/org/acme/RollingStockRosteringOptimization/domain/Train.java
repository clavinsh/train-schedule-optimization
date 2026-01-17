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

    // The depot assigned to this train
    private Depo depo;

    // Current station where the train is located
    private Station currentStation;

    // Current number of passengers on the train
    private int currentPassengerCount;

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

    public Train(String id, int capacity, Depo depo) {
        this.id = id;
        this.capacity = capacity;
        this.depo = depo;
    }

    /**
     * Check if this train can accommodate the given number of additional passengers.
     */
    public boolean canAccommodate(int additionalPassengers) {
        return currentPassengerCount + additionalPassengers <= capacity;
    }

    /**
     * Check if the train has exceeded its capacity.
     */
    public boolean isOverCapacity() {
        return currentPassengerCount > capacity;
    }

    /**
     * Get the remaining capacity of this train.
     */
    public int getRemainingCapacity() {
        return Math.max(0, capacity - currentPassengerCount);
    }

    /**
     * Board passengers onto the train (up to remaining capacity).
     * Returns the number of passengers actually boarded.
     */
    public int boardPassengers(int passengerCount) {
        int canBoard = Math.min(passengerCount, getRemainingCapacity());
        currentPassengerCount += canBoard;
        return canBoard;
    }

    /**
     * Alight all passengers from the train.
     */
    public void alightAllPassengers() {
        currentPassengerCount = 0;
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

    public Depo getDepo() {
        return depo;
    }

    public void setDepo(Depo depo) {
        this.depo = depo;
    }

    public Station getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(Station currentStation) {
        this.currentStation = currentStation;
    }

    public int getCurrentPassengerCount() {
        return currentPassengerCount;
    }

    public void setCurrentPassengerCount(int currentPassengerCount) {
        this.currentPassengerCount = currentPassengerCount;
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
