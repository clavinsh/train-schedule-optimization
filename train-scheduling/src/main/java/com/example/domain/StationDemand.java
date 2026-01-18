package com.example.domain;

import java.time.LocalTime;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents passenger demand (embark & disembark) for a specific station on a specific route for a
 * specific hour of the day
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class StationDemand {
    @PlanningId
    private Long id;
    private Station station;
    private Route route;
    private LocalTime time;

    private int embarkingPassengers;
    private int disembarkingPassengers;

    // Simulates passenger gradual arrival throughout the hour
    // At minute 0: 0 passengers have arrived at the station
    // At minute 30: half have arrived
    // At minute 59: all passengers
    public int getEmbarkingPassengers(LocalTime arrivalTime) {
        return lerpPassengersOnTime(arrivalTime, embarkingPassengers);
    }

    // Simulates passenger gradual arrival throughout the hour
    // At minute 0: 0 passengers have arrived at the station
    // At minute 30: half have arrived
    // At minute 59: all passengers
    public int getDisembarkingPassengers(LocalTime arrivalTime) {
        return lerpPassengersOnTime(arrivalTime, disembarkingPassengers);
    }

    // Linear interpolation (LERP) for passengers based on arrival time (minutes)
    private int lerpPassengersOnTime(LocalTime arrivalTime, int passengers) {
        // Incorrect hour
        if (arrivalTime.getHour() != time.getHour()) {
            return 0;
        }

        int arrivalMinutes = arrivalTime.getMinute();

        return (int) Math.round(passengers * (1 + arrivalMinutes) / 60.0);
    }
}
