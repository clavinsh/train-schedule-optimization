package com.example.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PassengerPoolState {
    private Station station;
    private Direction direction;

    private double carryOverPassengers;
    private LocalDateTime lastServiceTime;

    // Soft-score statistics
    private double totalPassengersServed;
    private double totalPassengersLeftBehind;
    private double totalWaitingMinutes;

    /**
     * Calculate current waiting passengers at a specific time,
     * including carry-over from previous trains and new arrivals.
     */
    public double getCurrentWaitingAt(LocalDateTime time, List<StationDemand> demands) {
        if (demands == null || time == null) {
            return carryOverPassengers;
        }

        LocalTime timeOfDay = time.toLocalTime();
        LocalTime lastServiceTimeOfDay = lastServiceTime != null ? lastServiceTime.toLocalTime() : null;

        double newArrivals = demands.stream()
                .filter(d -> d.getStation().equals(station) && d.getDirection().equals(direction))
                .mapToDouble(d -> {
                    if (lastServiceTimeOfDay == null) {
                        return d.getAccumulatedPassengersAt(timeOfDay);
                    }
                    return d.getPassengersBetween(lastServiceTimeOfDay, timeOfDay);
                })
                .sum();

        return carryOverPassengers + newArrivals;
    }

    /**
     * Record a train visit, updating state based on how many passengers were served.
     */
    public void recordTrainVisit(LocalDateTime visitTime, double passengersServed, double passengersLeftBehind) {
        this.lastServiceTime = visitTime;
        this.carryOverPassengers = passengersLeftBehind;
        this.totalPassengersServed += passengersServed;
        this.totalPassengersLeftBehind += passengersLeftBehind;
    }

    /**
     * Reset state for a new simulation run.
     */
    public void reset() {
        this.carryOverPassengers = 0;
        this.lastServiceTime = null;
        this.totalPassengersServed = 0;
        this.totalPassengersLeftBehind = 0;
        this.totalWaitingMinutes = 0;
    }
}
