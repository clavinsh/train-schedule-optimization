package com.example.domain;

import java.util.Set;

import com.example.domain.helpers.CoordinateCalc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Station {
    private int id;
    private String name;

    private double latitude;
    private double longitude;

    private boolean depot;
    private int platformCapacity;
    private int stopDurationInSeconds;

    private Set<Connection> connections;

    public double distanceTo(Station other) {
        return CoordinateCalc.haversineDistance(
                this.latitude, this.longitude,
                other.getLatitude(), other.getLongitude());
    }

    public boolean isConnectedTo(Station other) {
        if (connections == null) {
            return false;
        }
        return connections.stream()
                .anyMatch(conn -> conn.connects(this, other));
    }

    public Connection getConnectionTo(Station other) {
        if (connections == null) {
            return null;
        }
        return connections.stream()
                .filter(conn -> conn.connects(this, other))
                .findFirst()
                .orElse(null);
    }
}
