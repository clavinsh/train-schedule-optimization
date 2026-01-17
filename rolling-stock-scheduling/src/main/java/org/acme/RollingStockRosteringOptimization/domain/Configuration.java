package org.acme.RollingStockRosteringOptimization.domain;

import java.time.LocalTime;

/**
 * Global configuration parameters for the rolling stock optimization.
 * Contains constants that affect scheduling constraints and calculations.
 */
public class Configuration {

    // Minimal time between trains at the same station (in minutes) to prevent collisions
    private int minTimeBetweenTrainsMinutes;

    // Time required at each depot to board passengers (in minutes)
    private int boardingTimeMinutes;

    // Start of working hours
    private LocalTime workingHoursStart;

    // End of working hours
    private LocalTime workingHoursEnd;

    // Constant train speed in km/h
    private double trainSpeedKmh;

    public Configuration() {
        // Default values
        this.minTimeBetweenTrainsMinutes = 5;
        this.boardingTimeMinutes = 3;
        this.workingHoursStart = LocalTime.of(5, 0);
        this.workingHoursEnd = LocalTime.of(23, 59);
        this.trainSpeedKmh = 80.0;
    }

    public Configuration(int minTimeBetweenTrainsMinutes, int boardingTimeMinutes,
                         LocalTime workingHoursStart, LocalTime workingHoursEnd,
                         double trainSpeedKmh) {
        this.minTimeBetweenTrainsMinutes = minTimeBetweenTrainsMinutes;
        this.boardingTimeMinutes = boardingTimeMinutes;
        this.workingHoursStart = workingHoursStart;
        this.workingHoursEnd = workingHoursEnd;
        this.trainSpeedKmh = trainSpeedKmh;
    }

    /**
     * Calculate travel time in minutes for a given distance.
     */
    public double calculateTravelTimeMinutes(double distanceKm) {
        return (distanceKm / trainSpeedKmh) * 60;
    }

    /**
     * Check if a given time is within working hours.
     */
    public boolean isWithinWorkingHours(LocalTime time) {
        return !time.isBefore(workingHoursStart) && !time.isAfter(workingHoursEnd);
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public int getMinTimeBetweenTrainsMinutes() {
        return minTimeBetweenTrainsMinutes;
    }

    public void setMinTimeBetweenTrainsMinutes(int minTimeBetweenTrainsMinutes) {
        this.minTimeBetweenTrainsMinutes = minTimeBetweenTrainsMinutes;
    }

    public int getBoardingTimeMinutes() {
        return boardingTimeMinutes;
    }

    public void setBoardingTimeMinutes(int boardingTimeMinutes) {
        this.boardingTimeMinutes = boardingTimeMinutes;
    }

    public LocalTime getWorkingHoursStart() {
        return workingHoursStart;
    }

    public void setWorkingHoursStart(LocalTime workingHoursStart) {
        this.workingHoursStart = workingHoursStart;
    }

    public LocalTime getWorkingHoursEnd() {
        return workingHoursEnd;
    }

    public void setWorkingHoursEnd(LocalTime workingHoursEnd) {
        this.workingHoursEnd = workingHoursEnd;
    }

    public double getTrainSpeedKmh() {
        return trainSpeedKmh;
    }

    public void setTrainSpeedKmh(double trainSpeedKmh) {
        this.trainSpeedKmh = trainSpeedKmh;
    }
}
