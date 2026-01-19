package com.example.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TrackOccupancy {
    private int id;

    private Connection connection;
    private TrackDirection direction;

    private Train train;
    private AbstractTrip trip;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    /**
     * Check if this occupancy overlaps in time with another occupancy.
     * Two intervals [a1, a2] and [b1, b2] overlap if a1 < b2 AND b1 < a2.
     */
    public boolean overlaps(TrackOccupancy other) {
        if (entryTime == null || exitTime == null ||
                other.getEntryTime() == null || other.getExitTime() == null) {
            return false;
        }
        return entryTime.isBefore(other.getExitTime()) &&
                other.getEntryTime().isBefore(exitTime);
    }

    /**
     * Check if this occupancy conflicts with another (potential collision).
     * A conflict occurs when:
     * 1. Same connection
     * 2. Opposite directions on a single track
     * 3. Time overlap exists
     */
    public boolean conflictsWith(TrackOccupancy other) {
        if (connection == null || other.getConnection() == null) {
            return false;
        }
        // Must be same connection
        if (!connection.equals(other.getConnection())) {
            return false;
        }
        // Double track can handle both directions simultaneously
        if (connection.isDoubleTrack()) {
            return false;
        }
        // Opposite directions on single track with time overlap
        return direction != other.getDirection() && overlaps(other);
    }

    /**
     * Get the duration of this track occupancy in seconds.
     */
    public long getOccupancyDurationSeconds() {
        if (entryTime == null || exitTime == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(entryTime, exitTime);
    }
}
