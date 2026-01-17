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

    // Current number of passengers on the train
    private int currentPassengerCount;

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
     * Get the remaining capacity of this train.
     */
    public int getRemainingCapacity() {
        return Math.max(0, capacity - currentPassengerCount);
    }

    /**
     * Board passengers onto the train (for simulation purposes).
     * @param count the number of passengers to board
     * @return the number of passengers that actually boarded (limited by remaining capacity)
     */
    public int boardPassengers(int count) {
        int canBoard = Math.min(count, getRemainingCapacity());
        currentPassengerCount += canBoard;
        return canBoard;
    }

    /**
     * Alight passengers from the train (for simulation purposes).
     * @param count the number of passengers to alight
     * @return the number of passengers that actually alighted
     */
    public int alightPassengers(int count) {
        int toAlight = Math.min(count, currentPassengerCount);
        currentPassengerCount -= toAlight;
        return toAlight;
    }

    /**
     * Alight all passengers from the train.
     */
    public void alightAllPassengers() {
        currentPassengerCount = 0;
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

    public int getCurrentPassengerCount() {
        return currentPassengerCount;
    }

    public void setCurrentPassengerCount(int currentPassengerCount) {
        this.currentPassengerCount = currentPassengerCount;
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
