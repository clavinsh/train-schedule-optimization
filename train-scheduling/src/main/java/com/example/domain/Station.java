package com.example.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.domain.helpers.CoordinateCalc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
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

    // Cache: stationId -> Connection for O(1) lookup
    private transient Map<Integer, Connection> connectionByStationId;

    public Station(int id, String name, double latitude, double longitude,
                   boolean depot, int platformCapacity, int stopDurationInSeconds,
                   Set<Connection> connections) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depot = depot;
        this.platformCapacity = platformCapacity;
        this.stopDurationInSeconds = stopDurationInSeconds;
        this.connections = connections;
        initConnectionCache();
    }

    /**
     * Initialize the connection lookup cache. Call after setting connections.
     */
    public void initConnectionCache() {
        connectionByStationId = new HashMap<>();
        if (connections != null) {
            for (Connection conn : connections) {
                Station other = conn.getA().equals(this) ? conn.getB() : conn.getA();
                connectionByStationId.put(other.getId(), conn);
            }
        }
    }

    public double distanceTo(Station other) {
        return CoordinateCalc.haversineDistance(
                this.latitude, this.longitude,
                other.getLatitude(), other.getLongitude());
    }

    public boolean isConnectedTo(Station other) {
        return getConnectionTo(other) != null;
    }

    /**
     * O(1) connection lookup using cached map.
     */
    public Connection getConnectionTo(Station other) {
        if (connectionByStationId == null) {
            initConnectionCache();
        }
        return connectionByStationId.get(other.getId());
    }
}
