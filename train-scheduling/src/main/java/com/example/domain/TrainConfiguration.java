package com.example.domain;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration parameters for the train scheduling system.
 * Maps to "Konfigurācija" from the domain model.
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class TrainConfiguration {
    // Minimum time interval between two trains going to the same station
    private Duration minIntervalBetweenTrains;

    // How long a train stops at each station
    private Duration stationStopDuration;

    // Average train speed in km/h (used to calculate travel time between stations)
    private double averageSpeedKmPerHour;


    /**
     * Calculates the travel time between two stations based on distance and average speed.
     */
    public Duration calculateTravelTime(double distanceKm) {
        if (averageSpeedKmPerHour <= 0) {
            return Duration.ZERO;
        }
        double hours = distanceKm / averageSpeedKmPerHour;
        long minutes = Math.round(hours * 60);
        return Duration.ofMinutes(minutes);
    }
}
