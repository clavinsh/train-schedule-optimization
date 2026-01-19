package com.example.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Route {
    private int id;
    private String name;

    private List<Station> stops;

    private int turnaroundTimeSeconds; // time at terminus before reverse

    // Pre-computed cached data (call initCache() after setting stops)
    private transient List<Station> forwardStops;
    private transient List<Station> reverseStops;
    private transient List<Connection> forwardConnections;
    private transient List<Connection> reverseConnections;
    private transient int baseTravelTimeSecondsForward;
    private transient int baseTravelTimeSecondsReverse;

    public Route(int id, String name, List<Station> stops, int turnaroundTimeSeconds) {
        this.id = id;
        this.name = name;
        this.stops = stops;
        this.turnaroundTimeSeconds = turnaroundTimeSeconds;
        initCache();
    }

    /**
     * Initialize cached computed values. Call this after setting stops.
     */
    public void initCache() {
        if (stops == null || stops.isEmpty()) {
            forwardStops = Collections.emptyList();
            reverseStops = Collections.emptyList();
            forwardConnections = Collections.emptyList();
            reverseConnections = Collections.emptyList();
            baseTravelTimeSecondsForward = 0;
            baseTravelTimeSecondsReverse = 0;
            return;
        }

        // Cache forward stops (immutable copy)
        forwardStops = Collections.unmodifiableList(new ArrayList<>(stops));

        // Cache reverse stops (immutable copy)
        List<Station> reversed = new ArrayList<>(stops);
        Collections.reverse(reversed);
        reverseStops = Collections.unmodifiableList(reversed);

        // Cache connections and compute total travel times
        forwardConnections = computeConnections(forwardStops);
        reverseConnections = computeConnections(reverseStops);

        baseTravelTimeSecondsForward = forwardConnections.stream()
                .mapToInt(Connection::getBaseTravelTimeSeconds).sum();
        baseTravelTimeSecondsReverse = reverseConnections.stream()
                .mapToInt(Connection::getBaseTravelTimeSeconds).sum();
    }

    private List<Connection> computeConnections(List<Station> orderedStops) {
        if (orderedStops.size() < 2) {
            return Collections.emptyList();
        }
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < orderedStops.size() - 1; i++) {
            Connection conn = orderedStops.get(i).getConnectionTo(orderedStops.get(i + 1));
            if (conn != null) {
                connections.add(conn);
            }
        }
        return Collections.unmodifiableList(connections);
    }

    public Station getFirstStop(Direction direction) {
        List<Station> stopsInDir = getStopsInDirection(direction);
        return stopsInDir.isEmpty() ? null : stopsInDir.get(0);
    }

    public Station getLastStop(Direction direction) {
        List<Station> stopsInDir = getStopsInDirection(direction);
        return stopsInDir.isEmpty() ? null : stopsInDir.get(stopsInDir.size() - 1);
    }

    /**
     * Returns cached stops list - no allocation.
     */
    public List<Station> getStopsInDirection(Direction direction) {
        if (forwardStops == null) {
            initCache();
        }
        return direction == Direction.FORWARD ? forwardStops : reverseStops;
    }

    public int getTotalStops() {
        return stops == null ? 0 : stops.size();
    }

    /**
     * Returns cached connections list - no allocation.
     */
    public List<Connection> getRequiredConnections(Direction direction) {
        if (forwardConnections == null) {
            initCache();
        }
        return direction == Direction.FORWARD ? forwardConnections : reverseConnections;
    }

    /**
     * Get base travel time (without train speed adjustment) for the entire route.
     */
    public int getBaseTravelTimeSeconds(Direction direction) {
        if (forwardConnections == null) {
            initCache();
        }
        return direction == Direction.FORWARD ? baseTravelTimeSecondsForward : baseTravelTimeSecondsReverse;
    }
}
