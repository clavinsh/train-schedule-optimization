package com.example.domain;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a train trip running an entire route from start to end.
 * This is the core planning entity that Timefold optimizes.
 *
 * Each RouteDeparture represents a potential trip slot - the solver assigns:
 * - Which train runs this trip
 * - What time the trip departs from the first station
 *
 * Arrival times at intermediate stations are derived from the departure time.
 */
@Setter
@Getter
@NoArgsConstructor
@PlanningEntity(comparatorClass = RouteDeparture.DifficultyComparator.class)
public class RouteDeparture {

    /**
     * Difficulty comparator for construction heuristic.
     * Longer routes are harder to schedule, so they should be assigned first.
     */
    public static class DifficultyComparator implements Comparator<RouteDeparture> {
        @Override
        public int compare(RouteDeparture a, RouteDeparture b) {
            int aStations = a.getRoute() != null ? a.getRoute().getStations().size() : 0;
            int bStations = b.getRoute() != null ? b.getRoute().getStations().size() : 0;
            return Integer.compare(bStations, aStations); // Longer routes first (descending)
        }
    }

    @PlanningId
    private Long id;

    // Problem fact - the route this trip covers (fixed, not changed by solver)
    private Route route;

    // Demand lookup: Station -> (Hour -> StationDemand)
    // Populated during data generation for efficient demand calculation
    @JsonIgnore
    private Map<Station, Map<Integer, StationDemand>> stationDemandLookup;

    // Planning variables - Timefold will optimize these
    @PlanningVariable(valueRangeProviderRefs = "trainRange")
    private Train train;

    @PlanningVariable(valueRangeProviderRefs = "timeRange")
    private LocalTime departureTime;

    // Configuration: minutes per station (used for arrival time calculation)
    private static final int MINUTES_PER_STATION = 3;

    public RouteDeparture(Long id, Route route) {
        this.id = id;
        this.route = route;
    }

    public RouteDeparture(Long id, Route route, Train train, LocalTime departureTime) {
        this.id = id;
        this.route = route;
        this.train = train;
        this.departureTime = departureTime;
    }

    // ========== Derived Properties (computed from departure time) ==========

    /**
     * Gets the first station of the route (departure station)
     */
    @JsonIgnore
    public Station getFirstStation() {
        if (route == null || route.getStations().isEmpty()) {
            return null;
        }
        return route.getStations().get(0);
    }

    /**
     * Gets the last station of the route (arrival station)
     */
    @JsonIgnore
    public Station getLastStation() {
        if (route == null || route.getStations().isEmpty()) {
            return null;
        }
        List<Station> stations = route.getStations();
        return stations.get(stations.size() - 1);
    }

    /**
     * Calculates the arrival time at a specific station index on the route.
     * Assumes MINUTES_PER_STATION minutes travel time between each station.
     *
     * @param stationIndex 0-based index of the station in the route
     * @return arrival time at that station, or null if departure time not set
     */
    public LocalTime getArrivalTimeAt(int stationIndex) {
        if (departureTime == null || route == null) {
            return null;
        }
        if (stationIndex < 0 || stationIndex >= route.getStations().size()) {
            return null;
        }
        return departureTime.plusMinutes((long) stationIndex * MINUTES_PER_STATION);
    }

    /**
     * Calculates the arrival time at the last station (trip end time).
     */
    @JsonIgnore
    public LocalTime getEndTime() {
        if (departureTime == null || route == null) {
            return null;
        }
        int stationCount = route.getStations().size();
        return departureTime.plusMinutes((long) (stationCount - 1) * MINUTES_PER_STATION);
    }

    /**
     * Gets the total trip duration in minutes.
     */
    @JsonIgnore
    public int getTripDurationMinutes() {
        if (route == null) {
            return 0;
        }
        return (route.getStations().size() - 1) * MINUTES_PER_STATION;
    }

    /**
     * Checks if this trip overlaps in time with another trip.
     * Two trips overlap if one starts before the other ends.
     */
    public boolean overlaps(RouteDeparture other) {
        if (this.departureTime == null || other.departureTime == null) {
            return false;
        }
        LocalTime thisEnd = this.getEndTime();
        LocalTime otherEnd = other.getEndTime();

        if (thisEnd == null || otherEnd == null) {
            return false;
        }

        // Convert to minutes for comparison
        int thisStart = this.departureTime.getHour() * 60 + this.departureTime.getMinute();
        int thisEndMin = thisEnd.getHour() * 60 + thisEnd.getMinute();
        int otherStart = other.departureTime.getHour() * 60 + other.departureTime.getMinute();
        int otherEndMin = otherEnd.getHour() * 60 + otherEnd.getMinute();

        // Check overlap: not (this ends before other starts OR other ends before this starts)
        return !(thisEndMin <= otherStart || otherEndMin <= thisStart);
    }

    /**
     * Checks if this trip can be followed by another trip for the same train.
     * The train must end at the same station where the next trip starts.
     */
    public boolean canBeFollowedBy(RouteDeparture next) {
        if (this.route == null || next.route == null) {
            return false;
        }
        Station thisLastStation = this.getLastStation();
        Station nextFirstStation = next.getFirstStation();

        if (thisLastStation == null || nextFirstStation == null) {
            return false;
        }

        return thisLastStation.equals(nextFirstStation);
    }

    // ========== Demand Calculation Methods ==========

    /**
     * Calculates total embarking passengers across ALL stations on this route
     * for the current departure time. Considers the arrival time at each station.
     *
     * @return total embarking passengers, or 0 if departure time not set
     */
    public int getTotalEmbarkingPassengers() {
        if (departureTime == null || route == null || stationDemandLookup == null) {
            return 0;
        }

        int total = 0;
        List<Station> stations = route.getStations();

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            LocalTime arrivalTime = getArrivalTimeAt(i);

            if (arrivalTime != null) {
                StationDemand demand = getDemandForStation(station, arrivalTime.getHour());
                if (demand != null) {
                    total += demand.getEmbarkingPassengers(arrivalTime);
                }
            }
        }

        return total;
    }

    /**
     * Calculates total disembarking passengers across ALL stations on this route
     * for the current departure time. Considers the arrival time at each station.
     *
     * @return total disembarking passengers, or 0 if departure time not set
     */
    public int getTotalDisembarkingPassengers() {
        if (departureTime == null || route == null || stationDemandLookup == null) {
            return 0;
        }

        int total = 0;
        List<Station> stations = route.getStations();

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            LocalTime arrivalTime = getArrivalTimeAt(i);

            if (arrivalTime != null) {
                StationDemand demand = getDemandForStation(station, arrivalTime.getHour());
                if (demand != null) {
                    total += demand.getDisembarkingPassengers(arrivalTime);
                }
            }
        }

        return total;
    }

    /**
     * Calculates the maximum passenger load at any point during the trip.
     * This tracks cumulative passengers (embarking - disembarking) at each station.
     *
     * @return maximum passenger count on the train at any station, or 0 if not set
     */
    public int getMaxPassengerLoad() {
        if (departureTime == null || route == null || stationDemandLookup == null) {
            return 0;
        }

        int currentLoad = 0;
        int maxLoad = 0;
        List<Station> stations = route.getStations();

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            LocalTime arrivalTime = getArrivalTimeAt(i);

            if (arrivalTime != null) {
                StationDemand demand = getDemandForStation(station, arrivalTime.getHour());
                if (demand != null) {
                    // Passengers disembark first, then new passengers embark
                    currentLoad -= demand.getDisembarkingPassengers(arrivalTime);
                    currentLoad = Math.max(0, currentLoad); // Can't go negative
                    currentLoad += demand.getEmbarkingPassengers(arrivalTime);
                    maxLoad = Math.max(maxLoad, currentLoad);
                }
            }
        }

        return maxLoad;
    }

    /**
     * Gets the demand for a specific station at a specific hour.
     *
     * @param station the station to look up
     * @param hour the hour (0-23)
     * @return the StationDemand, or null if not found
     */
    @JsonIgnore
    public StationDemand getDemandForStation(Station station, int hour) {
        if (stationDemandLookup == null) {
            return null;
        }
        Map<Integer, StationDemand> hourlyDemand = stationDemandLookup.get(station);
        if (hourlyDemand == null) {
            return null;
        }
        return hourlyDemand.get(hour);
    }

    /**
     * Checks if the maximum passenger load exceeds train capacity.
     *
     * @return true if capacity is exceeded at any point, false otherwise
     */
    public boolean exceedsCapacity() {
        if (train == null) {
            return false;
        }
        return getMaxPassengerLoad() > train.getCapacity();
    }

    /**
     * Gets the number of passengers exceeding capacity (for penalty calculation).
     *
     * @return number of passengers over capacity, or 0 if within capacity
     */
    public int getCapacityOverflow() {
        if (train == null) {
            return 0;
        }
        return Math.max(0, getMaxPassengerLoad() - train.getCapacity());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteDeparture that = (RouteDeparture) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RouteDeparture{" +
                "id=" + id +
                ", route=" + (route != null ? route.getName() : "null") +
                ", train=" + (train != null ? train.getId() : "unassigned") +
                ", departure=" + (departureTime != null ? departureTime : "unassigned") +
                '}';
    }
}
