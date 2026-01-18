package com.example.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.domain.Depo;
import com.example.domain.RollingStockSchedule;
import com.example.domain.Route;
import com.example.domain.Station;
import com.example.domain.Train;
import com.example.domain.TrainConfiguration;
import com.example.domain.Trip;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DemoDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataGenerator.class);
    private static final Random RANDOM = new Random(37);

    public RollingStockSchedule generateDemoData() {
        return generateDataset(6, 22, 2);
    }

    private static RollingStockSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        TrainConfiguration configuration = new TrainConfiguration(
                Duration.ofMinutes(5),
                Duration.ofMinutes(2),
                80.0 // 80 km/h average speed
        );

        Map<Long, Station> stationMap = new HashMap<>();
        List<Station> stations = generateStations(stationMap, configuration);

        List<Route> routes = generateRoutes(stationMap);
        List<Train> trains = generateTrains(6);
        List<Depo> depos = generateDepos(stationMap, trains);
        
        // Assign home depos to trains
        for (int i = 0; i < trains.size(); i++) {
            trains.get(i).setHomeDepo(depos.get(i % depos.size()));
        }

        List<Trip> trips = generateTrips(routes);

        List<LocalTime> availableDepartureTimes = generateAvailableDepartureTimes(startHour, endHour);

        return new RollingStockSchedule(trains, stations, routes, depos, configuration, availableDepartureTimes, trips, null, null);
    }

    private static List<LocalTime> generateAvailableDepartureTimes(int startHour, int endHour) {
        List<LocalTime> times = new ArrayList<>();
        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 5) { // 5-minute intervals
                times.add(LocalTime.of(hour, minute));
            }
        }
        return times;
    }

    private static List<Station> generateStations(Map<Long, Station> stationMap, TrainConfiguration configuration) {
        List<Station> stations = new ArrayList<>();
        // Simplified station list for brevity
        createStation(stationMap, stations, 1L, "Riga", 56.9496, 24.1052);
        createStation(stationMap, stations, 2L, "Jelgava", 56.6530, 23.7130);
        createStation(stationMap, stations, 3L, "Liepaja", 56.5050, 21.0110);
        createStation(stationMap, stations, 4L, "Daugavpils", 55.8750, 26.5360);
        createStation(stationMap, stations, 5L, "Valmiera", 57.5380, 25.4270);
        createStation(stationMap, stations, 6L, "Rezekne", 56.5100, 27.3400);

        // Populate demand and travel times
        for (Station from : stations) {
            Map<Integer, Integer> embarkingDemand = new HashMap<>();
            Map<Integer, Integer> disembarkingDemand = new HashMap<>();
            Map<Long, Duration> travelTimes = new HashMap<>();

            for (int hour = 6; hour <= 22; hour++) {
                int[] demand = generateDemand(hour);
                embarkingDemand.put(hour, demand[0]);
                disembarkingDemand.put(hour, demand[1]);
            }
            from.setEmbarkingDemand(embarkingDemand);
            from.setDisembarkingDemand(disembarkingDemand);

            for (Station to : stations) {
                if (!from.equals(to)) {
                    double distance = from.distanceTo(to);
                    travelTimes.put(to.getId(), configuration.calculateTravelTime(distance));
                }
            }
            from.setTravelTimesToNeighbors(travelTimes);
        }
        return stations;
    }
    
    private static void createStation(Map<Long, Station> stationMap, List<Station> stations, Long id, String name, double lat, double lon) {
        Station station = new Station(id, name, lat, lon, new HashMap<>(), new HashMap<>(), new HashMap<>());
        stationMap.put(id, station);
        stations.add(station);
    }

    private static List<Route> generateRoutes(Map<Long, Station> stationMap) {
        List<Route> routes = new ArrayList<>();
        // Main routes
        routes.add(new Route(1L, "Riga-Liepaja", List.of(stationMap.get(1L), stationMap.get(2L), stationMap.get(3L))));
        routes.add(new Route(2L, "Liepaja-Riga", List.of(stationMap.get(3L), stationMap.get(2L), stationMap.get(1L))));
        routes.add(new Route(3L, "Riga-Daugavpils", List.of(stationMap.get(1L), stationMap.get(4L))));
        routes.add(new Route(4L, "Daugavpils-Riga", List.of(stationMap.get(4L), stationMap.get(1L))));
        // Additional routes
        routes.add(new Route(5L, "Riga-Valmiera", List.of(stationMap.get(1L), stationMap.get(5L))));
        routes.add(new Route(6L, "Valmiera-Riga", List.of(stationMap.get(5L), stationMap.get(1L))));
        routes.add(new Route(7L, "Daugavpils-Rezekne", List.of(stationMap.get(4L), stationMap.get(6L))));
        routes.add(new Route(8L, "Rezekne-Daugavpils", List.of(stationMap.get(6L), stationMap.get(4L))));
        return routes;
    }

    private static List<Train> generateTrains(int count) {
        List<Train> trains = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            trains.add(new Train((long) i, 200 + (i * 10), null, null, null));
        }
        return trains;
    }

    private static List<Depo> generateDepos(Map<Long, Station> stationMap, List<Train> trains) {
        List<Depo> depos = new ArrayList<>();
        // One depot at each major station
        depos.add(new Depo(1L, stationMap.get(1L), trains.size())); // Riga
        depos.add(new Depo(2L, stationMap.get(3L), trains.size())); // Liepaja
        depos.add(new Depo(3L, stationMap.get(4L), trains.size())); // Daugavpils
        depos.add(new Depo(4L, stationMap.get(5L), trains.size())); // Valmiera
        depos.add(new Depo(5L, stationMap.get(6L), trains.size())); // Rezekne
        return depos;
    }
    
    private static int[] generateDemand(int hour) {
        int embarking, disembarking;
        if (hour >= 7 && hour <= 9) { // Morning rush
            embarking = 50 + RANDOM.nextInt(100);
            disembarking = 20 + RANDOM.nextInt(40);
        } else if (hour >= 17 && hour <= 19) { // Evening rush
            embarking = 30 + RANDOM.nextInt(50);
            disembarking = 60 + RANDOM.nextInt(80);
        } else { // Off-peak
            embarking = 10 + RANDOM.nextInt(30);
            disembarking = 10 + RANDOM.nextInt(30);
        }
        return new int[]{embarking, disembarking};
    }

    private static List<Trip> generateTrips(List<Route> routes) {
        List<Trip> trips = new ArrayList<>();
        long tripId = 1L;
        int servicesPerRoute = 6; // Multiple services per route throughout the day

        // Create multiple services for each route
        for (Route route : routes) {
            for (int service = 0; service < servicesPerRoute; service++) {
                // Each service covers all stations in the route
                for (int stationIdx = 0; stationIdx < route.getStations().size(); stationIdx++) {
                    trips.add(new Trip(tripId++, route, stationIdx));
                }
            }
        }
        return trips;
    }

    /**
     * Prints a summary of the schedule to the console for debugging.
     */
    public static void printScheduleSummary(RollingStockSchedule schedule) {
        LOGGER.info("========== SCHEDULE SUMMARY ==========");
        LOGGER.info("Trains: {}", schedule.getTrains().size());
        LOGGER.info("Stations: {}", schedule.getStations().size());
        LOGGER.info("Routes: {}", schedule.getRoutes().size());
        LOGGER.info("Depos: {}", schedule.getDepos().size());
        LOGGER.info("Trips: {}", schedule.getTrips().size());
        LOGGER.info("Available departure times: {}", schedule.getAvailableDepartureTimes().size());

        LOGGER.info("--- Trains ---");
        for (var train : schedule.getTrains()) {
            String homeDepoStation = train.getHomeDepo() != null ? train.getHomeDepo().getStation().getName() : "None";
            LOGGER.info("  Train {}: capacity={}, homeDepo={}", train.getId(), train.getCapacity(), homeDepoStation);
        }

        LOGGER.info("--- Routes ---");
        for (var route : schedule.getRoutes()) {
            List<String> stationNames = route.getStations().stream()
                    .map(Station::getName)
                    .toList();
            LOGGER.info("  Route {}: {} -> {}", route.getId(), route.getName(), stationNames);
        }

        LOGGER.info("--- Trips by Route ---");
        Map<Long, Long> tripsPerRoute = new HashMap<>();
        for (var trip : schedule.getTrips()) {
            tripsPerRoute.merge(trip.getRoute().getId(), 1L, Long::sum);
        }
        for (var entry : tripsPerRoute.entrySet()) {
            LOGGER.info("  Route {}: {} trips", entry.getKey(), entry.getValue());
        }

        LOGGER.info("=======================================");
    }
}
