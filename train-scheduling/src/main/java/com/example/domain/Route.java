package com.example.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Route {
    private int id;
    private String name;

    private List<Station> stops;

    private int turnaroundTimeSeconds; // time at terminus before reverse

    public Station getFirstStop(Direction direction) {
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        return direction == Direction.FORWARD ? stops.get(0) : stops.get(stops.size() - 1);
    }

    public Station getLastStop(Direction direction) {
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        return direction == Direction.FORWARD ? stops.get(stops.size() - 1) : stops.get(0);
    }

    public List<Station> getStopsInDirection(Direction direction) {
        if (stops == null) {
            return Collections.emptyList();
        }
        if (direction == Direction.FORWARD) {
            return new ArrayList<>(stops);
        }
        List<Station> reversed = new ArrayList<>(stops);
        Collections.reverse(reversed);
        return reversed;
    }

    public int getTotalStops() {
        return stops == null ? 0 : stops.size();
    }

    public List<Connection> getRequiredConnections(Direction direction) {
        List<Station> orderedStops = getStopsInDirection(direction);
        if (orderedStops.size() < 2) {
            return Collections.emptyList();
        }

        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < orderedStops.size() - 1; i++) {
            Station from = orderedStops.get(i);
            Station to = orderedStops.get(i + 1);
            Connection conn = from.getConnectionTo(to);
            if (conn != null) {
                connections.add(conn);
            }
        }
        return connections;
    }
}
