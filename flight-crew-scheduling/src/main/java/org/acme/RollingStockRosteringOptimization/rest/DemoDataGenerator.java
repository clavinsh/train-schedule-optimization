package org.acme.RollingStockRosteringOptimization.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.RollingStockRosteringOptimization.domain.Configuration;
import org.acme.RollingStockRosteringOptimization.domain.Demand;
import org.acme.RollingStockRosteringOptimization.domain.Depo;
import org.acme.RollingStockRosteringOptimization.domain.Ride;
import org.acme.RollingStockRosteringOptimization.domain.RollingStockSchedule;
import org.acme.RollingStockRosteringOptimization.domain.Route;
import org.acme.RollingStockRosteringOptimization.domain.Station;
import org.acme.RollingStockRosteringOptimization.domain.TimeTable;
import org.acme.RollingStockRosteringOptimization.domain.Train;

@ApplicationScoped
public class DemoDataGenerator {

    private static final double DISTANCE_BETWEEN_STATIONS_KM = 10.0;
    private static final int[] TRAIN_CAPACITIES = {500, 500, 500, 500, 500};
    private static final int RIDES_PER_ROUTE_MIN = 8;
    private static final int RIDES_PER_ROUTE_MAX = 12;
    private static final int TRAIN_COUNT = 40;

    // Depot locations (hard-coded as these are infrastructure decisions)
    private static final List<String> DEPOT_STATIONS = List.of("Rīga", "Krustpils", "Jelgava");

    // Major hubs with higher passenger demand
    private static final Set<String> MAJOR_HUBS = Set.of(
            "Rīga", "Krustpils", "Jelgava", "Daugavpils", "Rēzekne",
            "Liepāja", "Sigulda", "Cēsis", "Valmiera", "Ogre", "Majori", "Gulbene"
    );

    // Route definitions - the single source of truth for all station names and route structures
    private static final List<RouteDefinition> ROUTE_DEFINITIONS = List.of(
            new RouteDefinition("Rīga - Indra", List.of(
                    "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                    "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                    "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                    "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                    "Trepe", "Līvāni", "Jersika", "Nīcgale", "Vabole", "Līksna", "Daugavpils", "Krāslava", "Indra")),
            new RouteDefinition("Rīga - Zilupe", List.of(
                    "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                    "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                    "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                    "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                    "Kūkas", "Mežāre", "Atašiene", "Stirniene", "Varakļāni", "Viļāni", "Sakstagals",
                    "Rēzekne", "Taudejāni", "Cirma", "Ludza", "Istalsna", "Nerza", "Briģi", "Zilupe")),
            new RouteDefinition("Rīga - Gulbene", List.of(
                    "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                    "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                    "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                    "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                    "Jaunkalsnava", "Kalsnava", "Mārciena", "Madona", "Cesvaine", "Jaungulbene", "Gulbene")),
            new RouteDefinition("Rīga - Liepāja", List.of(
                    "Rīga", "Torņakalns", "Bieriņi / Bērnu slimnīca", "BA Turība", "Tīraine",
                    "Medemciems", "Jaunolaine", "Olaine", "Dalbe", "Cena", "Ozolnieki", "Cukurfabrika",
                    "Jelgava", "Dobele", "Biksti", "Saldus", "Skrunda", "Liepāja")),
            new RouteDefinition("Rīga - Tukums II", List.of(
                    "Rīga", "Torņakalns", "Zasulauks", "Depo", "Zolitūde", "Imanta", "Babīte",
                    "Priedaine", "Lielupe", "Bulduri", "Dzintari", "Majori", "Dubulti", "Jaundubulti",
                    "Pumpuri", "Melluži", "Asari", "Vaivari", "Sloka", "Kūdra", "Ķemeri", "Smārde",
                    "Milzkalne", "Tukums I", "Tukums II")),
            new RouteDefinition("Rīga - Valga", List.of(
                    "Rīga", "Zemitāni", "Čiekurkalns", "Šmerlis", "Jugla", "Garkalne", "Krievupe",
                    "Vangaži", "Inčukalns", "Egļupe", "Sigulda", "Līgatne", "Ieriķi", "Melturi",
                    "Āraiši", "Cēsis", "Jānamuiža", "Lode", "Valmiera", "Strenči", "Lugaži", "Valga")),
            new RouteDefinition("Rīga - Skulte", List.of(
                    "Rīga", "Zemitāni", "Brasa", "Sarkandaugava", "Dauderi", "Mangaļi", "Ziemeļblāzma",
                    "Vecdaugava", "Vecāķi", "Kalngale", "Garciems", "Garupe", "Carnikava", "Gauja",
                    "Lilaste", "Inčupe", "Pabaži", "Saulkrasti", "Ķīšupe", "Zvejniekciems", "Skulte"))
    );

    private record RouteDefinition(String name, List<String> stationNames) {}

    public RollingStockSchedule generateDemoData() {
        Random random = new Random(42);
        RollingStockSchedule schedule = new RollingStockSchedule();

        // Create all stations
        Map<String, Station> stationMap = createStations();
        List<Station> stations = new ArrayList<>(stationMap.values());

        // Set up neighbor distances (bidirectional, 10km each)
        setupNeighborDistances(stationMap);

        // Create routes
        List<Route> routes = createRoutes(stationMap);

        // Create depots at major hubs
        List<Depo> depos = createDepos(stationMap);

        // Create trains
        List<Train> trains = createTrains(depos, random);

        // Create configuration
        Configuration configuration = new Configuration();

        // Create rides for each route
        List<Ride> allRides = new ArrayList<>();
        List<TimeTable> timeTables = new ArrayList<>();
        AtomicInteger rideCounter = new AtomicInteger(1);
        AtomicInteger timeTableCounter = new AtomicInteger(1);

        for (Route route : routes) {
            int ridesForRoute = RIDES_PER_ROUTE_MIN + random.nextInt(RIDES_PER_ROUTE_MAX - RIDES_PER_ROUTE_MIN + 1);
            List<Ride> routeRides = createRidesForRoute(route, ridesForRoute, rideCounter, configuration, random);
            allRides.addAll(routeRides);

            TimeTable timeTable = new TimeTable(
                    String.valueOf(timeTableCounter.getAndIncrement()),
                    route,
                    routeRides
            );
            timeTables.add(timeTable);
        }

        // Create demand for major stations
        List<Demand> demands = createDemands(stationMap, random);

        // Set all data on schedule
        schedule.setStations(stations);
        schedule.setDepos(depos);
        schedule.setRoutes(routes);
        schedule.setTrains(trains);
        schedule.setRides(allRides);
        schedule.setDemands(demands);
        schedule.setTimeTables(timeTables);
        schedule.setConfiguration(configuration);

        return schedule;
    }

    /**
     * Print a summary of the generated schedule data for debugging.
     */
    public void printScheduleSummary(RollingStockSchedule schedule) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ROLLING STOCK SCHEDULE - DEMO DATA SUMMARY");
        System.out.println("=".repeat(80));

        // Stations
        System.out.println("\n--- STATIONS (" + schedule.getStations().size() + " total) ---");
        System.out.println("First 10 stations:");
        schedule.getStations().stream().limit(10)
                .forEach(s -> System.out.println("  - " + s.getName()));
        if (schedule.getStations().size() > 10) {
            System.out.println("  ... and " + (schedule.getStations().size() - 10) + " more");
        }

        // Depots
        System.out.println("\n--- DEPOTS (" + schedule.getDepos().size() + " total) ---");
        schedule.getDepos().forEach(d ->
            System.out.println("  - " + d.getId() + " at " + d.getStation().getName()));

        // Routes
        System.out.println("\n--- ROUTES (" + schedule.getRoutes().size() + " total) ---");
        for (Route route : schedule.getRoutes()) {
            System.out.println("  - " + route.getName() + " (" + route.getStations().size() + " stations, "
                    + route.getSegmentCount() + " segments)");
        }

        // Trains
        System.out.println("\n--- TRAINS (" + schedule.getTrains().size() + " total) ---");
        System.out.println("Capacity distribution:");
        schedule.getTrains().stream()
                .collect(java.util.stream.Collectors.groupingBy(Train::getCapacity, java.util.stream.Collectors.counting()))
                .forEach((capacity, count) -> System.out.println("  - Capacity " + capacity + ": " + count + " trains"));

        // Rides
        System.out.println("\n--- RIDES (" + schedule.getRides().size() + " total) ---");
        System.out.println("Rides per route:");
        schedule.getRides().stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getRoute().getName(), java.util.stream.Collectors.counting()))
                .forEach((routeName, count) -> System.out.println("  - " + routeName + ": " + count + " rides"));

        // Sample rides
        System.out.println("\nSample rides (first 5):");
        schedule.getRides().stream().limit(5).forEach(r ->
            System.out.println("  - " + r.getId() + ": " + r.getDepartureStation().getName()
                    + " -> " + r.getArrivalStation().getName()
                    + " @ " + r.getDepartureTime().toLocalTime() + "-" + r.getArrivalTime().toLocalTime()));

        // Demands
        System.out.println("\n--- DEMANDS (" + schedule.getDemands().size() + " stations with demand) ---");
        System.out.println("Sample demand (major hubs at rush hour 8:00):");
        schedule.getDemands().stream()
                .filter(d -> MAJOR_HUBS.contains(d.getStation().getName()))
                .limit(5)
                .forEach(d -> System.out.println("  - " + d.getStation().getName() + ": " + d.getDemandAtHour(8) + " passengers"));

        // Total demand calculation
        int totalDemandAt8 = schedule.getDemands().stream()
                .mapToInt(d -> d.getDemandAtHour(8))
                .sum();
        int totalCapacity = schedule.getTrains().stream()
                .mapToInt(Train::getCapacity)
                .sum();
        System.out.println("\n--- CAPACITY vs DEMAND (at 8:00 AM) ---");
        System.out.println("  Total train capacity: " + totalCapacity + " passengers");
        System.out.println("  Total demand at 8AM: " + totalDemandAt8 + " passengers");
        System.out.println("  Ratio: " + String.format("%.2f", (double) totalCapacity / totalDemandAt8));

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    private Map<String, Station> createStations() {
        // Collect unique station names from all route definitions (preserving order)
        Set<String> stationNames = new LinkedHashSet<>();
        for (RouteDefinition routeDef : ROUTE_DEFINITIONS) {
            stationNames.addAll(routeDef.stationNames());
        }

        // Create Station objects
        Map<String, Station> stations = new HashMap<>();
        AtomicInteger idCounter = new AtomicInteger(1);
        for (String name : stationNames) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        return stations;
    }

    private void setupNeighborDistances(Map<String, Station> stationMap) {
        // Derive all neighbor connections from route definitions
        // Consecutive stations in any route are neighbors
        for (RouteDefinition routeDef : ROUTE_DEFINITIONS) {
            List<String> stationNames = routeDef.stationNames();
            for (int i = 0; i < stationNames.size() - 1; i++) {
                connectStations(stationMap, stationNames.get(i), stationNames.get(i + 1));
            }
        }
    }

    private void connectStations(Map<String, Station> stationMap, String fromName, String toName) {
        Station fromStation = stationMap.get(fromName);
        Station toStation = stationMap.get(toName);
        if (fromStation != null && toStation != null) {
            if (fromStation.getNeighborDistances() == null) {
                fromStation.setNeighborDistances(new HashMap<>());
            }
            if (toStation.getNeighborDistances() == null) {
                toStation.setNeighborDistances(new HashMap<>());
            }
            fromStation.getNeighborDistances().put(toStation, DISTANCE_BETWEEN_STATIONS_KM);
            toStation.getNeighborDistances().put(fromStation, DISTANCE_BETWEEN_STATIONS_KM);
        }
    }

    private List<Route> createRoutes(Map<String, Station> stationMap) {
        List<Route> routes = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        for (RouteDefinition routeDef : ROUTE_DEFINITIONS) {
            List<Station> stations = new ArrayList<>();
            for (String stationName : routeDef.stationNames()) {
                Station station = stationMap.get(stationName);
                if (station != null) {
                    stations.add(station);
                }
            }
            routes.add(new Route(String.valueOf(idCounter.getAndIncrement()), routeDef.name(), stations));
        }

        return routes;
    }

    private List<Depo> createDepos(Map<String, Station> stationMap) {
        List<Depo> depos = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        for (String stationName : DEPOT_STATIONS) {
            Station station = stationMap.get(stationName);
            if (station != null) {
                depos.add(new Depo(String.valueOf(idCounter.getAndIncrement()), station));
            }
        }

        return depos;
    }

    private List<Train> createTrains(List<Depo> depos, Random random) {
        List<Train> trains = new ArrayList<>();

        for (int i = 1; i <= TRAIN_COUNT; i++) {
            int capacity = TRAIN_CAPACITIES[random.nextInt(TRAIN_CAPACITIES.length)];
            Depo depo = depos.get(random.nextInt(depos.size()));
            trains.add(new Train(String.valueOf(i), capacity, depo));
        }

        return trains;
    }

    private List<Ride> createRidesForRoute(Route route, int rideCount, AtomicInteger rideCounter,
                                           Configuration config, Random random) {
        List<Ride> rides = new ArrayList<>();
        List<Station> stations = route.getStations();

        if (stations.size() < 2) {
            return rides;
        }

        // Generate rides throughout the day (5:00 to 22:00)
        LocalDateTime baseDate = LocalDateTime.now().withHour(5).withMinute(0).withSecond(0).withNano(0);

        // Calculate travel time per segment (10km at 80km/h = 7.5 minutes)
        double travelTimePerSegmentMinutes = config.calculateTravelTimeMinutes(DISTANCE_BETWEEN_STATIONS_KM);

        for (int rideNum = 0; rideNum < rideCount; rideNum++) {
            // Spread rides throughout the day
            int startHour = 5 + (rideNum * 17 / rideCount); // Spread from 5:00 to 22:00
            int startMinute = random.nextInt(60);
            LocalDateTime departureTime = baseDate.withHour(startHour).withMinute(startMinute);

            // Create a ride for each segment of the route
            for (int segmentIdx = 0; segmentIdx < stations.size() - 1; segmentIdx++) {
                Station departureStation = stations.get(segmentIdx);
                Station arrivalStation = stations.get(segmentIdx + 1);

                // Calculate arrival time
                LocalDateTime arrivalTime = departureTime.plusMinutes((long) travelTimePerSegmentMinutes);

                Ride ride = new Ride(
                        String.valueOf(rideCounter.getAndIncrement()),
                        route,
                        departureStation,
                        arrivalStation,
                        departureTime,
                        arrivalTime
                );
                rides.add(ride);

                // Next segment starts when this one ends (plus boarding time)
                departureTime = arrivalTime.plusMinutes(config.getBoardingTimeMinutes());
            }
        }

        return rides;
    }

    private List<Demand> createDemands(Map<String, Station> stationMap, Random random) {
        List<Demand> demands = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        // Generate demand for ALL stations
        for (Station station : stationMap.values()) {
            String stationName = station.getName();
            boolean isMajorHub = MAJOR_HUBS.contains(stationName);
            boolean isCapital = "Rīga".equals(stationName);

            Map<Integer, Integer> hourlyDemand = new HashMap<>();

            // Generate demand for each hour (5:00 to 23:00)
            for (int hour = 5; hour <= 23; hour++) {
                int baseDemand;
                boolean isRushHour = (hour >= 7 && hour <= 9) || (hour >= 16 && hour <= 18);

                if (isCapital) {
                    // Highest demand for capital city
                    baseDemand = 200 + random.nextInt(300);
                } else if (isMajorHub) {
                    // Major hubs have higher demand
                    if (isRushHour) {
                        baseDemand = 100 + random.nextInt(150);
                    } else {
                        baseDemand = 30 + random.nextInt(70);
                    }
                } else {
                    // Regular stations have lower demand
                    if (isRushHour) {
                        baseDemand = 20 + random.nextInt(40);
                    } else {
                        baseDemand = 5 + random.nextInt(20);
                    }
                }
                hourlyDemand.put(hour, baseDemand);
            }

            demands.add(new Demand(String.valueOf(idCounter.getAndIncrement()), station, hourlyDemand));
        }

        return demands;
    }
}
