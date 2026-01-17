package org.acme.RollingStockRosteringOptimization.domain;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents passenger demand at a specific station for each hour of the day.
 * Time intervals range from 00:00 to 23:59 with hourly granularity.
 */
@JsonIdentityInfo(scope = Demand.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Demand {

    @PlanningId
    private String id;

    // The station this demand is for
    private Station station;

    // Map of hour (0-23) to number of passengers demanding transport at that hour
    private Map<Integer, Integer> hourlyDemand;

    public Demand() {
    }

    public Demand(String id) {
        this.id = id;
    }

    public Demand(String id, Station station) {
        this.id = id;
        this.station = station;
    }

    public Demand(String id, Station station, Map<Integer, Integer> hourlyDemand) {
        this.id = id;
        this.station = station;
        this.hourlyDemand = hourlyDemand;
    }

    /**
     * Get the passenger demand for a specific hour.
     */
    public int getDemandAtHour(int hour) {
        if (hourlyDemand == null || !hourlyDemand.containsKey(hour)) {
            return 0;
        }
        return hourlyDemand.get(hour);
    }

    /**
     * Get the passenger demand for a specific time.
     */
    public int getDemandAtTime(LocalTime time) {
        return getDemandAtHour(time.getHour());
    }

    /**
     * Get total demand across all hours.
     */
    public int getTotalDemand() {
        if (hourlyDemand == null) {
            return 0;
        }
        return hourlyDemand.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public String toString() {
        return "Demand@" + (station != null ? station.getName() : "?");
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

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Map<Integer, Integer> getHourlyDemand() {
        return hourlyDemand;
    }

    public void setHourlyDemand(Map<Integer, Integer> hourlyDemand) {
        this.hourlyDemand = hourlyDemand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Demand demand))
            return false;
        return Objects.equals(getId(), demand.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
