package com.example.domain;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StationDemand {
    private int id;
    private Station station;
    private Direction direction;

    // time window of passenger demand
    private LocalTime hourStart;
    private LocalTime hourEnd;

    private int passengersPerHour;

    /**
     * Get accumulated passengers at a specific time using linear interpolation.
     * For example, if passengersPerHour is 150 and time is 30 minutes into the hour,
     * returns 75.
     */
    public double getAccumulatedPassengersAt(LocalTime time) {
        if (time.isBefore(hourStart)) {
            return 0;
        }
        if (!time.isBefore(hourEnd)) {
            return passengersPerHour;
        }

        long minutesIntoWindow = ChronoUnit.MINUTES.between(hourStart, time);
        long windowDurationMinutes = ChronoUnit.MINUTES.between(hourStart, hourEnd);

        if (windowDurationMinutes <= 0) {
            return passengersPerHour;
        }

        return passengersPerHour * ((double) minutesIntoWindow / windowDurationMinutes);
    }

    /**
     * Get passengers that arrived between two times within this demand window.
     */
    public double getPassengersBetween(LocalTime from, LocalTime to) {
        if (to.isBefore(hourStart) || from.isAfter(hourEnd) || !from.isBefore(to)) {
            return 0;
        }

        LocalTime effectiveFrom = from.isBefore(hourStart) ? hourStart : from;
        LocalTime effectiveTo = to.isAfter(hourEnd) ? hourEnd : to;

        return getAccumulatedPassengersAt(effectiveTo) - getAccumulatedPassengersAt(effectiveFrom);
    }

    /**
     * Get the arrival rate per minute.
     */
    public double getArrivalRatePerMinute() {
        long windowDurationMinutes = ChronoUnit.MINUTES.between(hourStart, hourEnd);
        if (windowDurationMinutes <= 0) {
            return 0;
        }
        return (double) passengersPerHour / windowDurationMinutes;
    }
}
