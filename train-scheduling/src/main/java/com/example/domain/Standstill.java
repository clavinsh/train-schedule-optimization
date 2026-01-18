package com.example.domain;

import java.time.LocalTime;

/**
 * Common interface for entities that can be the previous standstill in a trip chain.
 * Both Train and Trip implement this interface.
 */
public interface Standstill {
    LocalTime getDepartureTime();
    Station getStation();
    Trip getNextTrip();
    void setNextTrip(Trip nextTrip);
}
