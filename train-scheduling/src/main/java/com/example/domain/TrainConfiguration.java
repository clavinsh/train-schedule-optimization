package com.example.domain;

import java.time.Duration;

/**
 * Configuration parameters for the train scheduling system.
 * Maps to "Konfigurācija" from the domain model.
 */
public class TrainConfiguration {

    // Minimum time interval between two trains going to the same station
    private Duration minIntervalBetweenTrains;

    // How long a train stops at each station
    private Duration stationStopDuration;

    // Average train speed in km/h (used to calculate travel time between stations)
    private double averageSpeedKmPerHour;

    // No-arg constructor
    public TrainConfiguration() {
    }

    public TrainConfiguration(Duration minIntervalBetweenTrains, Duration stationStopDuration,
            double averageSpeedKmPerHour) {
        this.minIntervalBetweenTrains = minIntervalBetweenTrains;
        this.stationStopDuration = stationStopDuration;
        this.averageSpeedKmPerHour = averageSpeedKmPerHour;
    }

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

    // Getters and setters

    public Duration getMinIntervalBetweenTrains() {
        return minIntervalBetweenTrains;
    }

    public void setMinIntervalBetweenTrains(Duration minIntervalBetweenTrains) {
        this.minIntervalBetweenTrains = minIntervalBetweenTrains;
    }

    public Duration getStationStopDuration() {
        return stationStopDuration;
    }

    public void setStationStopDuration(Duration stationStopDuration) {
        this.stationStopDuration = stationStopDuration;
    }

    public double getAverageSpeedKmPerHour() {
        return averageSpeedKmPerHour;
    }

    public void setAverageSpeedKmPerHour(double averageSpeedKmPerHour) {
        this.averageSpeedKmPerHour = averageSpeedKmPerHour;
    }
}
