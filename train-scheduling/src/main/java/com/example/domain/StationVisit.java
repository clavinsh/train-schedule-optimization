package com.example.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StationVisit {
    private int id;

    private ScheduledTrip trip;
    private Station station;
    private int sequenceIdx;

    // Times (shadow calculated)
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private int dwellTimeSeconds;

    // Passenger data (calculated during simulation)
    private double passengersWaitingAtArrival;
    private double passengersBoarded;
    private double passengersAlighted;
    private double passengersLeftBehind;
    private double passengerLoadAfter; // on train after this stop

    public boolean isFirstStop() {
        return sequenceIdx == 0;
    }

    public boolean isLastStop() {
        if (trip == null || trip.getRoute() == null) {
            return false;
        }
        return sequenceIdx == trip.getRoute().getTotalStops() - 1;
    }

    /**
     * Get time since last service at this station for this direction.
     * Returns 0 if this is the first stop or if previous visit cannot be determined.
     */
    public long getWaitTimeForPassengers() {
        // This would require access to all station visits to determine
        // the previous train's visit time at this station.
        // For now, return dwell time as a placeholder.
        // Full implementation requires the constraint/simulation layer.
        return dwellTimeSeconds;
    }
}
