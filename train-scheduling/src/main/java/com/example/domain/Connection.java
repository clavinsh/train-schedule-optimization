package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Connection {
    private int id;
    private Station a;
    private Station b;

    private double distanceKm;
    private int baseTravelTimeSeconds;
    private boolean doubleTrack;


    public Station getOtherEnd(Station from) {
        if (from.equals(a)) {
            return b;
        } else if (from.equals(b)) {
            return a;
        }
        // Not sure if a hacky solution, but perhaps this can be swept under invalid data or request
        throw new IllegalArgumentException(
                "Station " + from.getId() + " is not part of this connection");
    }

    public TrackDirection getDirectionFrom(Station from) {
        if (from.equals(a)) {
            return TrackDirection.A_B;
        } else if (from.equals(b)) {
            return TrackDirection.B_A;
        }
        throw new IllegalArgumentException(
                "Station " + from.getId() + " is not part of this connection");
    }

    public boolean connects(Station s1, Station s2) {
        return (a.equals(s1) && b.equals(s2)) || (a.equals(s2) && b.equals(s1));
    }

    public int getTravelTimeForTrain(Train train) {
        return baseTravelTimeSeconds;
    }
}
