package com.example.domain;

import java.time.LocalTime;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Represents passenger demand (embark & disembark) for a specific station on a specific route for a
 * specific hour of the day
 */
public class StationDemand {

    @PlanningId
    private Long id;
    private Station station;
    private Route route;
    private LocalTime time;

    private int embarkingPassengers;
    private int disembarkingPassengers;

    // No-arg constructor required by Jackson
    public StationDemand() {
    }

    public StationDemand(Long id, Station station, Route route, LocalTime time,
            int embarkingPassengers, int disembarkingPassengers) {
        this.id = id;
        this.station = station;
        this.route = route;
        this.time = time;
        this.embarkingPassengers = embarkingPassengers;
        this.disembarkingPassengers = disembarkingPassengers;
    }

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime hour) {
        this.time = hour;
    }

    public int getEmbarkingPassengers() {
        return embarkingPassengers;
    }

    public void setEmbarkingPassengers(int embarkingPassengers) {
        this.embarkingPassengers = embarkingPassengers;
    }

    public int getDisembarkingPassengers() {
        return disembarkingPassengers;
    }

    public void setDisembarkingPassengers(int disembarkingPassengers) {
        this.disembarkingPassengers = disembarkingPassengers;
    }
}
