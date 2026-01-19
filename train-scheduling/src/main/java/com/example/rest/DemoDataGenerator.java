package com.example.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import com.example.domain.*;

/**
 * DemoDataGenerator - Generates test data for the Train Schedule Optimization problem
 *
 * Creates:
 * - Stations with coordinates (Latvian railway network)
 * - Connections between stations
 * - Routes connecting stations
 * - Trains with depots and capacities
 * - Hourly passenger demand
 * - Scheduled trips (planning entities)
 */
@ApplicationScoped
public class DemoDataGenerator {

    private static final Random RANDOM = new Random(37); // Fixed seed for reproducibility

    /**
     * Generates a complete TrainSchedule with demo data
     */
    public TrainSchedule generateDemoData() {
        return generateDefaultDataset();
    }

    /**
     * Prints sample instances from each domain class in the schedule
     */
    public static void printGeneratedData(TrainSchedule schedule) {
        int sampleSize = 3;

        System.out.println("=== GENERATED DEMO DATA ===\n");

        // Trains
        System.out.println("--- Trains (showing " + sampleSize + " of " + schedule.getTrains().size() + ") ---");
        schedule.getTrains().stream().limit(sampleSize).forEach(t ->
                System.out.println("  Train #" + t.getId() + " - capacity: " + t.getPassengerCapacity() +
                        ", depot: " + (t.getDepot() != null ? t.getDepot().getName() : "none")));
        System.out.println();

        // Stations
        System.out.println("--- Stations (showing " + sampleSize + " of " + schedule.getStations().size() + ") ---");
        schedule.getStations().stream().limit(sampleSize).forEach(s ->
                System.out.println("  Station #" + s.getId() + " - " + s.getName() +
                        " (" + s.getLatitude() + ", " + s.getLongitude() + ")" +
                        (s.isDepot() ? " [DEPOT]" : "")));
        System.out.println();

        // Connections
        System.out.println("--- Connections (showing " + sampleSize + " of " + schedule.getConnections().size() + ") ---");
        schedule.getConnections().stream().limit(sampleSize).forEach(c ->
                System.out.println("  Connection #" + c.getId() + " - " + c.getA().getName() +
                        " <-> " + c.getB().getName() +
                        " (" + String.format("%.1f", c.getDistanceKm()) + " km, " +
                        c.getBaseTravelTimeSeconds() / 60 + " min)"));
        System.out.println();

        // Routes
        System.out.println("--- Routes (showing " + sampleSize + " of " + schedule.getRoutes().size() + ") ---");
        schedule.getRoutes().stream().limit(sampleSize).forEach(r ->
                System.out.println("  Route #" + r.getId() + " - " + r.getName() +
                        " (" + r.getStops().size() + " stations)"));
        System.out.println();

        // StationDemands
        System.out.println("--- StationDemands (showing " + sampleSize + " of " +
                schedule.getStationDemands().size() + ") ---");
        schedule.getStationDemands().stream().limit(sampleSize).forEach(sd ->
                System.out.println("  Demand #" + sd.getId() + " - " + sd.getStation().getName() +
                        " " + sd.getDirection() + " at " + sd.getHourStart() + "-" + sd.getHourEnd() +
                        " (" + sd.getPassengersPerHour() + " passengers/hour)"));
        System.out.println();

        // ScheduledTrips
        System.out.println("--- ScheduledTrips (showing " + sampleSize + " of " +
                schedule.getScheduledTrips().size() + ") ---");
        schedule.getScheduledTrips().stream().limit(sampleSize).forEach(st ->
                System.out.println("  ScheduledTrip #" + st.getId() + " - " + st.getRoute().getName() +
                        " " + st.getDirection() +
                        " (train: " + (st.getAssignedTrain() != null ? st.getAssignedTrain().getId() : "unassigned") +
                        ", time: " + (st.getDepartureTime() != null ? st.getDepartureTime() : "unassigned") + ")"));
        System.out.println();

        System.out.println("=== END OF DEMO DATA ===");
    }

    /**
     * Generates default size dataset
     */
    public static TrainSchedule generateDefaultDataset() {
        return generateDataset(6, 22, 2); // 6 AM to 10 PM, every 2 hours
    }

    /**
     * Generates small dataset for quick testing
     */
    public static TrainSchedule generateSmallDataset() {
        return generateDataset(8, 18, 2); // 8 AM to 6 PM, every 2 hours
    }

    /**
     * Generates large dataset for stress testing
     */
    public static TrainSchedule generateLargeDataset() {
        return generateDataset(6, 22, 1); // 6 AM to 10 PM, every hour
    }

    /**
     * Generates dataset with configurable time range
     */
    private static TrainSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        // Create stations (returns map for easy lookup by ID)
        Map<Integer, Station> stationMap = new HashMap<>();
        List<Station> stations = generateStations(stationMap);

        // Create connections between stations
        List<Connection> connections = generateConnections(stationMap);

        // Link connections to stations
        linkConnectionsToStations(stations, connections);

        // Create routes
        List<Route> routes = generateRoutes(stationMap);

        // Create trains with depot assignments
        int trainCount = 4;
        List<Train> trains = generateTrains(trainCount, stationMap);

        // Create passenger demand
        List<StationDemand> stationDemands = generateStationDemands(stations, routes);

        // Create scheduled trips (planning entities)
        List<ScheduledTrip> scheduledTrips = generateScheduledTrips(routes, startHour, endHour, intervalHours);

        TrainSchedule schedule = new TrainSchedule();
        schedule.setStations(stations);
        schedule.setConnections(connections);
        schedule.setRoutes(routes);
        schedule.setTrains(trains);
        schedule.setStationDemands(stationDemands);
        schedule.setScheduledTrips(scheduledTrips);
        schedule.setScheduleDate(LocalDate.now());
        schedule.setScheduleName("Demo Schedule");
        // score is left null - Timefold will calculate it during solving
        return schedule;
    }

    /**
     * Generate stations in Latvia
     */
    private static List<Station> generateStations(Map<Integer, Station> stationMap) {
        List<Station> stations = new ArrayList<>();

        // Create all stations
        createStation(stationMap, stations, 1, "Riga", 56.9496, 24.1052, true);
        createStation(stationMap, stations, 2, "Vagonu parks", 56.9350, 24.1200, true);
        createStation(stationMap, stations, 3, "Tornakalns", 56.9350, 24.0800, false);
        createStation(stationMap, stations, 4, "Zemitani", 56.9600, 24.1300, false);
        createStation(stationMap, stations, 5, "Janavaarti", 56.9280, 24.1350, false);
        createStation(stationMap, stations, 6, "Daugmale", 56.9200, 24.1500, false);
        createStation(stationMap, stations, 7, "Skirotava", 56.9100, 24.1650, false);
        createStation(stationMap, stations, 8, "Gaisma", 56.9000, 24.1800, false);
        createStation(stationMap, stations, 9, "Rumbula", 56.8900, 24.1950, false);
        createStation(stationMap, stations, 10, "Darzini", 56.8800, 24.2100, false);
        createStation(stationMap, stations, 11, "Dole", 56.8700, 24.2250, false);
        createStation(stationMap, stations, 12, "Salaspils", 56.8600, 24.2400, false);
        createStation(stationMap, stations, 13, "Saulkalne", 56.8500, 24.2550, false);
        createStation(stationMap, stations, 14, "Ikskile", 56.8400, 24.2700, false);
        createStation(stationMap, stations, 15, "Jaunogre", 56.8300, 24.2850, false);
        createStation(stationMap, stations, 16, "Ogre", 56.8200, 24.6000, true);

        // Jelgava line
        createStation(stationMap, stations, 61, "Bierini", 56.9200, 24.0500, false);
        createStation(stationMap, stations, 62, "BA Turiba", 56.9100, 24.0300, false);
        createStation(stationMap, stations, 63, "Tiraine", 56.9000, 24.0100, false);
        createStation(stationMap, stations, 64, "Medemciems", 56.8800, 23.9800, false);
        createStation(stationMap, stations, 65, "Jaunolaine", 56.8500, 23.9500, false);
        createStation(stationMap, stations, 66, "Olaine", 56.7950, 23.9390, false);
        createStation(stationMap, stations, 67, "Dalbe", 56.7500, 23.9000, false);
        createStation(stationMap, stations, 68, "Cena", 56.7000, 23.8500, false);
        createStation(stationMap, stations, 69, "Ozolnieki", 56.6880, 23.7820, false);
        createStation(stationMap, stations, 70, "Cukurfabrika", 56.6500, 23.7300, false);
        createStation(stationMap, stations, 71, "Jelgava", 56.6530, 23.7130, true);

        // Jurmala line
        createStation(stationMap, stations, 87, "Zasulauks", 56.9300, 24.0600, false);
        createStation(stationMap, stations, 88, "Depo", 56.9250, 24.0400, false);
        createStation(stationMap, stations, 89, "Zolitude", 56.9380, 24.0520, false);
        createStation(stationMap, stations, 90, "Imanta", 56.9490, 24.0210, false);
        createStation(stationMap, stations, 91, "Babite", 56.9550, 23.9540, false);
        createStation(stationMap, stations, 92, "Priedaine", 56.9600, 23.9200, false);
        createStation(stationMap, stations, 93, "Lielupe", 56.9680, 23.8200, false);
        createStation(stationMap, stations, 94, "Bulduri", 56.9700, 23.7750, false);
        createStation(stationMap, stations, 95, "Dzintari", 56.9730, 23.7630, false);
        createStation(stationMap, stations, 96, "Majori", 56.9750, 23.7500, false);
        createStation(stationMap, stations, 97, "Dubulti", 56.9800, 23.7300, true);

        return stations;
    }

    private static void createStation(Map<Integer, Station> stationMap, List<Station> stations,
            int id, String name, double lat, double lon, boolean isDepot) {
        Station station = new Station();
        station.setId(id);
        station.setName(name);
        station.setLatitude(lat);
        station.setLongitude(lon);
        station.setDepot(isDepot);
        station.setPlatformCapacity(isDepot ? 5 : 2);
        station.setStopDurationInSeconds(120); // 2 minutes default stop
        station.setConnections(new HashSet<>());
        stationMap.put(id, station);
        stations.add(station);
    }

    /**
     * Generate connections between adjacent stations
     */
    private static List<Connection> generateConnections(Map<Integer, Station> stationMap) {
        List<Connection> connections = new ArrayList<>();
        AtomicInteger connectionId = new AtomicInteger(1);

        // Helper to create bidirectional connection
        java.util.function.BiConsumer<Integer, Integer> connect = (id1, id2) -> {
            Station s1 = stationMap.get(id1);
            Station s2 = stationMap.get(id2);
            if (s1 != null && s2 != null) {
                Connection conn = new Connection();
                conn.setId(connectionId.getAndIncrement());
                conn.setA(s1);
                conn.setB(s2);
                conn.setDistanceKm(s1.distanceTo(s2));
                conn.setBaseTravelTimeSeconds((int) (conn.getDistanceKm() * 60)); // ~1 min per km
                conn.setDoubleTrack(true); // Assume double track for main lines
                connections.add(conn);
            }
        };

        // Riga - Ogre line
        connect.accept(1, 2);
        connect.accept(2, 5);
        connect.accept(5, 6);
        connect.accept(6, 7);
        connect.accept(7, 8);
        connect.accept(8, 9);
        connect.accept(9, 10);
        connect.accept(10, 11);
        connect.accept(11, 12);
        connect.accept(12, 13);
        connect.accept(13, 14);
        connect.accept(14, 15);
        connect.accept(15, 16);

        // Riga - Jelgava line
        connect.accept(1, 3);
        connect.accept(3, 61);
        connect.accept(61, 62);
        connect.accept(62, 63);
        connect.accept(63, 64);
        connect.accept(64, 65);
        connect.accept(65, 66);
        connect.accept(66, 67);
        connect.accept(67, 68);
        connect.accept(68, 69);
        connect.accept(69, 70);
        connect.accept(70, 71);

        // Riga - Jurmala line (branches from Tornakalns)
        connect.accept(3, 87);
        connect.accept(87, 88);
        connect.accept(88, 89);
        connect.accept(89, 90);
        connect.accept(90, 91);
        connect.accept(91, 92);
        connect.accept(92, 93);
        connect.accept(93, 94);
        connect.accept(94, 95);
        connect.accept(95, 96);
        connect.accept(96, 97);

        return connections;
    }

    /**
     * Link connections to their respective stations
     */
    private static void linkConnectionsToStations(List<Station> stations, List<Connection> connections) {
        for (Connection conn : connections) {
            conn.getA().getConnections().add(conn);
            conn.getB().getConnections().add(conn);
        }
    }

    /**
     * Generate routes covering main lines
     */
    private static List<Route> generateRoutes(Map<Integer, Station> stationMap) {
        List<Route> routes = new ArrayList<>();
        AtomicInteger routeId = new AtomicInteger(1);

        // Helper to create route from station IDs
        java.util.function.BiFunction<String, Integer[], Route> createRoute = (name, stationIds) -> {
            List<Station> stops = new ArrayList<>();
            for (Integer id : stationIds) {
                Station station = stationMap.get(id);
                if (station != null) {
                    stops.add(station);
                }
            }
            Route route = new Route();
            route.setId(routeId.getAndIncrement());
            route.setName(name);
            route.setStops(stops);
            route.setTurnaroundTimeSeconds(300); // 5 minutes turnaround
            return route;
        };

        // Riga - Ogre
        routes.add(createRoute.apply("Riga-Ogre",
                new Integer[]{1, 2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}));

        // Riga - Jelgava
        routes.add(createRoute.apply("Riga-Jelgava",
                new Integer[]{1, 3, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71}));

        // Riga - Jurmala (Dubulti)
        routes.add(createRoute.apply("Riga-Jurmala",
                new Integer[]{1, 3, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97}));

        return routes;
    }

    /**
     * Generate trains with depot assignments
     */
    private static List<Train> generateTrains(int count, Map<Integer, Station> stationMap) {
        List<Train> trains = new ArrayList<>();

        // Depot stations
        Station rigaDepot = stationMap.get(1);  // Riga
        Station ogreDepot = stationMap.get(16); // Ogre
        Station jelgavaDepot = stationMap.get(71); // Jelgava
        Station dubultiDepot = stationMap.get(97); // Dubulti

        Station[] depots = {rigaDepot, ogreDepot, jelgavaDepot, dubultiDepot};

        for (int i = 0; i < count; i++) {
            Train train = new Train();
            train.setId(i + 1);
            train.setDepot(depots[i % depots.length]);
            train.setPassengerCapacity(150 + (i * 50)); // 150, 200, 250, 300...
            train.setSpeedFactor(1.0); // Normal speed
            trains.add(train);
        }

        return trains;
    }

    /**
     * Generate hourly passenger demand
     */
    private static List<StationDemand> generateStationDemands(List<Station> stations, List<Route> routes) {
        List<StationDemand> demands = new ArrayList<>();
        AtomicInteger id = new AtomicInteger(1);

        for (Station station : stations) {
            // Generate demand for both directions
            for (Direction direction : Direction.values()) {
                // Generate hourly demand from 6 AM to 10 PM
                for (int hour = 6; hour <= 22; hour++) {
                    int passengersPerHour = generateDemandForHour(hour);

                    StationDemand demand = new StationDemand();
                    demand.setId(id.getAndIncrement());
                    demand.setStation(station);
                    demand.setDirection(direction);
                    demand.setHourStart(LocalTime.of(hour, 0));
                    demand.setHourEnd(LocalTime.of(hour, 59));
                    demand.setPassengersPerHour(passengersPerHour);
                    demands.add(demand);
                }
            }
        }

        return demands;
    }

    /**
     * Generate realistic passenger demand based on time of day
     */
    private static int generateDemandForHour(int hour) {
        // Morning rush hour (7-9 AM)
        if (hour >= 7 && hour <= 9) {
            return 100 + RANDOM.nextInt(100);
        }
        // Evening rush hour (5-7 PM)
        else if (hour >= 17 && hour <= 19) {
            return 100 + RANDOM.nextInt(100);
        }
        // Mid-day
        else if (hour >= 10 && hour <= 16) {
            return 40 + RANDOM.nextInt(40);
        }
        // Early morning / late evening
        else {
            return 20 + RANDOM.nextInt(30);
        }
    }

    /**
     * Generate scheduled trips (planning entities)
     */
    private static List<ScheduledTrip> generateScheduledTrips(List<Route> routes,
            int startHour, int endHour, int intervalHours) {
        List<ScheduledTrip> trips = new ArrayList<>();
        AtomicInteger id = new AtomicInteger(1);

        for (Route route : routes) {
            // Generate trips for both directions
            for (Direction direction : Direction.values()) {
                // Generate one trip per time slot
                for (int hour = startHour; hour <= endHour; hour += intervalHours) {
                    ScheduledTrip trip = new ScheduledTrip();
                    trip.setId(id.getAndIncrement());
                    trip.setRoute(route);
                    trip.setDirection(direction);
                    // Train and departure time are null - Timefold will assign them
                    trips.add(trip);
                }
            }
        }

        return trips;
    }
}
