package com.example.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import jakarta.enterprise.context.ApplicationScoped;

import com.example.domain.*;

/**
 * DemoDataGenerator - Generates test data for the Rolling Stock Rostering problem
 *
 * Creates:
 * - Stations with coordinates (Latvian railway network)
 * - Routes connecting stations
 * - Trains with different capacities
 * - Depots at major stations
 * - Hourly passenger demand
 * - Departure times (planning entities)
 * - Available time slots for value range
 */
@ApplicationScoped
public class DemoDataGenerator {

    private static final Random RANDOM = new Random(37); // Fixed seed for reproducibility

    /**
     * Generates a complete RollingStockSchedule with demo data
     */
    public RollingStockSchedule generateDemoData() {
        return generateDefaultDataset();
    }

    /**
     * Prints sample instances from each domain class in the schedule
     */
    public static void printGeneratedData(RollingStockSchedule schedule) {
        int sampleSize = 3;

        System.out.println("=== GENERATED DEMO DATA ===\n");

        // Configuration
        TrainConfiguration config = schedule.getConfiguration();
        System.out.println("--- TrainConfiguration ---");
        System.out.println("  Min interval between trains: " + config.getMinIntervalBetweenTrains());
        System.out.println("  Station stop duration: " + config.getStationStopDuration());
        System.out.println("  Average speed: " + config.getAverageSpeedKmPerHour() + " km/h");
        System.out.println();

        // Trains
        System.out.println("--- Trains (showing " + sampleSize + " of " + schedule.getTrains().size() + ") ---");
        schedule.getTrains().stream().limit(sampleSize).forEach(t ->
                System.out.println("  Train #" + t.getId() + " - capacity: " + t.getCapacity()));
        System.out.println();

        // Stations
        System.out.println("--- Stations (showing " + sampleSize + " of " + schedule.getStations().size() + ") ---");
        schedule.getStations().stream().limit(sampleSize).forEach(s ->
                System.out.println("  Station #" + s.getId() + " - " + s.getName() +
                        " (" + s.getLatitude() + ", " + s.getLongitude() + ")"));
        System.out.println();

        // Routes
        System.out.println("--- Routes (showing " + sampleSize + " of " + schedule.getRoutes().size() + ") ---");
        schedule.getRoutes().stream().limit(sampleSize).forEach(r ->
                System.out.println("  Route #" + r.getId() + " - " + r.getName() +
                        " (" + r.getStations().size() + " stations)"));
        System.out.println();

        // Depos
        System.out.println("--- Depos (showing " + sampleSize + " of " + schedule.getDepos().size() + ") ---");
        schedule.getDepos().stream().limit(sampleSize).forEach(d ->
                System.out.println("  Depo #" + d.getId() + " at " + d.getStation().getName() +
                        " - capacity: " + d.getCapacity()));
        System.out.println();

        // TrainDepoAssignments
        System.out.println("--- TrainDepoAssignments (showing " + sampleSize + " of " +
                schedule.getTrainDepoAssignments().size() + ") ---");
        schedule.getTrainDepoAssignments().stream().limit(sampleSize).forEach(a ->
                System.out.println("  Assignment #" + a.getId() + " - Train #" + a.getTrain().getId() +
                        " -> " + a.getDepo()));
        System.out.println();

        // StationDemands
        System.out.println("--- StationDemands (showing " + sampleSize + " of " +
                schedule.getStationDemands().size() + ") ---");
        schedule.getStationDemands().stream().limit(sampleSize).forEach(sd ->
                System.out.println("  Demand #" + sd.getId() + " - " + sd.getStation().getName() +
                        " on " + sd.getRoute().getName() + " at " + sd.getTime() +
                        " (embarking: " + sd.getEmbarkingPassengers() +
                        ", disembarking: " + sd.getDisembarkingPassengers() + ")"));
        System.out.println();

        // DepartureTimes
        System.out.println("--- DepartureTimes (showing " + sampleSize + " of " +
                schedule.getDepartureTimes().size() + ") ---");
        schedule.getDepartureTimes().stream().limit(sampleSize).forEach(dt ->
                System.out.println("  DepartureTime #" + dt.getId() + " - " + dt.getStation().getName() +
                        " on " + dt.getRoute().getName() +
                        " (train: " + (dt.getTrain() != null ? dt.getTrain().getId() : "unassigned") +
                        ", time: " + (dt.getDepartureTime() != null ? dt.getDepartureTime() : "unassigned") + ")"));
        System.out.println();

        // Available departure times
        System.out.println("--- Available Departure Times (showing " + sampleSize + " of " +
                schedule.getAvailableDepartureTimes().size() + ") ---");
        schedule.getAvailableDepartureTimes().stream().limit(sampleSize).forEach(t ->
                System.out.println("  " + t));
        System.out.println();

        System.out.println("=== END OF DEMO DATA ===");
    }

    /**
     * Generates default size dataset
     */
    public static RollingStockSchedule generateDefaultDataset() {
        return generateDataset(6, 22, 2); // 6 AM to 10 PM, every 2 hours
    }

    /**
     * Generates small dataset for quick testing
     */
    public static RollingStockSchedule generateSmallDataset() {
        return generateDataset(8, 18, 2); // 8 AM to 6 PM, every 2 hours
    }

    /**
     * Generates large dataset for stress testing
     */
    public static RollingStockSchedule generateLargeDataset() {
        return generateDataset(6, 22, 1); // 6 AM to 10 PM, every hour
    }

    /**
     * Generates dataset with configurable time range
     */
    private static RollingStockSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        // Create configuration
        TrainConfiguration configuration = new TrainConfiguration(
                Duration.ofMinutes(5),  // Minimum 5 minutes between trains
                Duration.ofMinutes(2),  // 2 minutes station stop
                60.0                    // 60 km/h average speed
        );

        // Create stations (returns map for easy lookup by ID)
        Map<Long, Station> stationMap = new HashMap<>();
        List<Station> stations = generateStations(stationMap);

        // Create routes
        List<Route> routes = generateRoutes(stationMap);

        // Create trains
        // int trainCount = intervalHours == 1 ? 40 : 30;
        int trainCount = 4;
        List<Train> trains = generateTrains(trainCount);

        // Create depots
        List<Depo> depos = generateDepos(stationMap);

        // Create train-depo assignments
        List<TrainDepoAssignment> trainDepoAssignments = generateTrainDepoAssignments(trains, depos);

        // Create passenger demand
        List<StationDemand> stationDemands = generateStationDemands(stations, routes);

        // Create available departure times (value range for planning variable)
        List<LocalTime> availableDepartureTimes = generateAvailableDepartureTimes(startHour, endHour);

        // Create departure times (planning entities)
        List<DepartureTime> departureTimes = generateDepartureTimes(routes, stationDemands,
                startHour, endHour, intervalHours);

        RollingStockSchedule schedule = new RollingStockSchedule();
        schedule.setTrains(trains);
        schedule.setStations(stations);
        schedule.setRoutes(routes);
        schedule.setDepos(depos);
        schedule.setTrainDepoAssignments(trainDepoAssignments);
        schedule.setStationDemands(stationDemands);
        schedule.setConfiguration(configuration);
        schedule.setAvailableDepartureTimes(availableDepartureTimes);
        schedule.setDepartureTimes(departureTimes);
        // score is left null - Timefold will calculate it during solving
        return schedule;
    }

    /**
     * Generate available departure times for the value range provider
     * Creates time slots every 5 minutes from startHour to endHour
     */
    private static List<LocalTime> generateAvailableDepartureTimes(int startHour, int endHour) {
        List<LocalTime> times = new ArrayList<>();
        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 1) {
                times.add(LocalTime.of(hour, minute));
            }
        }
        return times;
    }

    /**
     * Generate stations in Latvia
     */
    private static List<Station> generateStations(Map<Long, Station> stationMap) {
        List<Station> stations = new ArrayList<>();

        // First pass: create all stations without neighbors
        createStation(stationMap, stations, 1L, "Riga", 56.9496, 24.1052);
        createStation(stationMap, stations, 2L, "Vagonu parks", 56.9350, 24.1200);
        createStation(stationMap, stations, 3L, "Tornakalns", 56.9350, 24.0800);
        createStation(stationMap, stations, 4L, "Zemitani", 56.9600, 24.1300);
        createStation(stationMap, stations, 5L, "Janavaarti", 56.9280, 24.1350);
        createStation(stationMap, stations, 6L, "Daugmale", 56.9200, 24.1500);
        createStation(stationMap, stations, 7L, "Skirotava", 56.9100, 24.1650);
        createStation(stationMap, stations, 8L, "Gaisma", 56.9000, 24.1800);
        createStation(stationMap, stations, 9L, "Rumbula", 56.8900, 24.1950);
        createStation(stationMap, stations, 10L, "Darzini", 56.8800, 24.2100);
        createStation(stationMap, stations, 11L, "Dole", 56.8700, 24.2250);
        createStation(stationMap, stations, 12L, "Salaspils", 56.8600, 24.2400);
        createStation(stationMap, stations, 13L, "Saulkalne", 56.8500, 24.2550);
        createStation(stationMap, stations, 14L, "Ikskile", 56.8400, 24.2700);
        createStation(stationMap, stations, 15L, "Jaunogre", 56.8300, 24.2850);
        createStation(stationMap, stations, 16L, "Ogre", 56.8200, 24.6000);
        createStation(stationMap, stations, 17L, "Parogre", 56.8100, 24.6150);
        createStation(stationMap, stations, 18L, "Ciemupe", 56.8000, 24.6300);
        createStation(stationMap, stations, 19L, "Kegums", 56.7900, 24.7000);
        createStation(stationMap, stations, 20L, "Lielvarde", 56.7800, 24.8000);
        createStation(stationMap, stations, 21L, "Kaibala", 56.7700, 24.8500);
        createStation(stationMap, stations, 22L, "Jumprava", 56.7600, 24.9000);
        createStation(stationMap, stations, 23L, "Skriveri", 56.7500, 25.0000);
        createStation(stationMap, stations, 24L, "Muldakmens", 56.7400, 25.1000);
        createStation(stationMap, stations, 25L, "Aizkraukle", 56.6050, 25.2550);
        createStation(stationMap, stations, 26L, "Koknese", 56.6500, 25.4300);
        createStation(stationMap, stations, 27L, "Alotene", 56.6800, 25.5000);
        createStation(stationMap, stations, 28L, "Plavinas", 56.6170, 25.7190);
        createStation(stationMap, stations, 29L, "Krustpils", 56.5095, 26.1840);
        createStation(stationMap, stations, 30L, "Trepe", 56.4900, 26.2500);
        createStation(stationMap, stations, 31L, "Livani", 56.3540, 26.1730);
        createStation(stationMap, stations, 32L, "Jersika", 56.2900, 26.3000);
        createStation(stationMap, stations, 33L, "Nicgale", 56.2500, 26.3500);
        createStation(stationMap, stations, 34L, "Vabole", 56.2000, 26.4000);
        createStation(stationMap, stations, 35L, "Liksna", 56.1500, 26.4500);
        createStation(stationMap, stations, 36L, "Daugavpils", 55.8750, 26.5360);
        createStation(stationMap, stations, 37L, "Kraslava", 55.8950, 27.1680);
        createStation(stationMap, stations, 38L, "Indra", 56.1000, 27.5000);
        createStation(stationMap, stations, 39L, "Kukas", 56.5500, 26.3000);
        createStation(stationMap, stations, 40L, "Mezare", 56.6000, 26.4000);
        createStation(stationMap, stations, 41L, "Atasiene", 56.6500, 26.5000);
        createStation(stationMap, stations, 42L, "Stirniene", 56.7000, 26.6000);
        createStation(stationMap, stations, 43L, "Varaklani", 56.6050, 26.7500);
        createStation(stationMap, stations, 44L, "Vilani", 56.5530, 26.9270);
        createStation(stationMap, stations, 45L, "Sakstagals", 56.5000, 27.1000);
        createStation(stationMap, stations, 46L, "Rezekne", 56.5100, 27.3400);
        createStation(stationMap, stations, 47L, "Taudejani", 56.5500, 27.5000);
        createStation(stationMap, stations, 48L, "Cirma", 56.6000, 27.6000);
        createStation(stationMap, stations, 49L, "Ludza", 56.5450, 27.7160);
        createStation(stationMap, stations, 50L, "Istalsna", 56.6000, 27.8000);
        createStation(stationMap, stations, 51L, "Nerza", 56.6500, 27.9000);
        createStation(stationMap, stations, 52L, "Brigi", 56.7000, 28.0000);
        createStation(stationMap, stations, 53L, "Zilupe", 56.3870, 28.1240);
        createStation(stationMap, stations, 54L, "Jaunkalsnava", 56.5500, 26.3500);
        createStation(stationMap, stations, 55L, "Kalsnava", 56.6000, 26.4500);
        createStation(stationMap, stations, 56L, "Marciena", 56.6800, 26.5500);
        createStation(stationMap, stations, 57L, "Madona", 56.8550, 26.2180);
        createStation(stationMap, stations, 58L, "Cesvaine", 56.9680, 26.3080);
        createStation(stationMap, stations, 59L, "Jaungulbene", 57.1000, 26.5000);
        createStation(stationMap, stations, 60L, "Gulbene", 57.1730, 26.7530);

        // Riga - Liepaja / Tukums line stations
        createStation(stationMap, stations, 61L, "Bierini", 56.9200, 24.0500);
        createStation(stationMap, stations, 62L, "BA Turiba", 56.9100, 24.0300);
        createStation(stationMap, stations, 63L, "Tiraine", 56.9000, 24.0100);
        createStation(stationMap, stations, 64L, "Medemciems", 56.8800, 23.9800);
        createStation(stationMap, stations, 65L, "Jaunolaine", 56.8500, 23.9500);
        createStation(stationMap, stations, 66L, "Olaine", 56.7950, 23.9390);
        createStation(stationMap, stations, 67L, "Dalbe", 56.7500, 23.9000);
        createStation(stationMap, stations, 68L, "Cena", 56.7000, 23.8500);
        createStation(stationMap, stations, 69L, "Ozolnieki", 56.6880, 23.7820);
        createStation(stationMap, stations, 70L, "Cukurfabrika", 56.6500, 23.7300);
        createStation(stationMap, stations, 71L, "Jelgava", 56.6530, 23.7130);
        createStation(stationMap, stations, 72L, "Dobele", 56.6250, 23.2800);
        createStation(stationMap, stations, 73L, "Biksti", 56.6000, 22.9000);
        createStation(stationMap, stations, 74L, "Saldus", 56.6650, 22.4930);
        createStation(stationMap, stations, 75L, "Skrunda", 56.6670, 22.0110);
        createStation(stationMap, stations, 76L, "Liepaja", 56.5050, 21.0110);

        // Jurmala / Tukums line stations
        createStation(stationMap, stations, 87L, "Zasulauks", 56.9300, 24.0600);
        createStation(stationMap, stations, 88L, "Depo", 56.9250, 24.0400);
        createStation(stationMap, stations, 89L, "Zolitude", 56.9380, 24.0520);
        createStation(stationMap, stations, 90L, "Imanta", 56.9490, 24.0210);
        createStation(stationMap, stations, 91L, "Babite", 56.9550, 23.9540);
        createStation(stationMap, stations, 92L, "Priedaine", 56.9600, 23.9200);
        createStation(stationMap, stations, 93L, "Lielupe", 56.9680, 23.8200);
        createStation(stationMap, stations, 94L, "Bulduri", 56.9700, 23.7750);
        createStation(stationMap, stations, 95L, "Dzintari", 56.9730, 23.7630);
        createStation(stationMap, stations, 96L, "Majori", 56.9750, 23.7500);
        createStation(stationMap, stations, 97L, "Dubulti", 56.9800, 23.7300);
        createStation(stationMap, stations, 98L, "Jaundubulti", 56.9850, 23.7100);
        createStation(stationMap, stations, 99L, "Pumpuri", 57.0000, 23.6800);
        createStation(stationMap, stations, 100L, "Melluzi", 57.0100, 23.6500);
        createStation(stationMap, stations, 101L, "Asari", 57.0200, 23.6200);
        createStation(stationMap, stations, 102L, "Vaivari", 57.0300, 23.5900);
        createStation(stationMap, stations, 103L, "Sloka", 57.0400, 23.5600);
        createStation(stationMap, stations, 104L, "Kudra", 57.0500, 23.5300);
        createStation(stationMap, stations, 105L, "Kemeri", 56.9960, 23.5160);
        createStation(stationMap, stations, 106L, "Smarde", 56.9800, 23.4500);
        createStation(stationMap, stations, 107L, "Milzkalne", 56.9600, 23.3800);
        createStation(stationMap, stations, 108L, "Tukums I", 56.9680, 23.1550);
        createStation(stationMap, stations, 109L, "Tukums II", 56.9700, 23.1400);

        // Valga line stations
        createStation(stationMap, stations, 110L, "Ciekurkalns", 56.9700, 24.1500);
        createStation(stationMap, stations, 111L, "Smerlis", 56.9850, 24.1700);
        createStation(stationMap, stations, 112L, "Jugla", 57.0280, 24.3260);
        createStation(stationMap, stations, 113L, "Garkalne", 57.0500, 24.4000);
        createStation(stationMap, stations, 114L, "Krievupe", 57.0800, 24.5000);
        createStation(stationMap, stations, 115L, "Vangazi", 57.0900, 24.5340);
        createStation(stationMap, stations, 116L, "Incukalns", 57.1000, 24.6860);
        createStation(stationMap, stations, 117L, "Eglupe", 57.1200, 24.7500);
        createStation(stationMap, stations, 118L, "Sigulda", 57.1540, 24.8530);
        createStation(stationMap, stations, 119L, "Ligatne", 57.2340, 25.0410);
        createStation(stationMap, stations, 120L, "Ieriki", 57.2800, 25.1500);
        createStation(stationMap, stations, 121L, "Melturi", 57.3000, 25.2000);
        createStation(stationMap, stations, 122L, "Araisi", 57.3100, 25.2700);
        createStation(stationMap, stations, 123L, "Cesis", 57.3120, 25.2670);
        createStation(stationMap, stations, 124L, "Janamuiza", 57.3500, 25.3500);
        createStation(stationMap, stations, 125L, "Lode", 57.4000, 25.4500);
        createStation(stationMap, stations, 126L, "Valmiera", 57.5380, 25.4270);
        createStation(stationMap, stations, 127L, "Strenci", 57.6260, 25.6840);
        createStation(stationMap, stations, 128L, "Lugazi", 57.7000, 25.9000);
        createStation(stationMap, stations, 129L, "Valga", 57.7780, 26.0460);

        // Skulte line stations
        createStation(stationMap, stations, 130L, "Brasa", 56.9800, 24.1600);
        createStation(stationMap, stations, 131L, "Sarkandaugava", 57.0000, 24.1800);
        createStation(stationMap, stations, 132L, "Dauderi", 57.0200, 24.2000);
        createStation(stationMap, stations, 133L, "Mangali", 57.0400, 24.2200);
        createStation(stationMap, stations, 134L, "Ziemelblazma", 57.0600, 24.2400);
        createStation(stationMap, stations, 135L, "Vecdaugava", 57.0800, 24.2600);
        createStation(stationMap, stations, 136L, "Vecaki", 57.0900, 24.2800);
        createStation(stationMap, stations, 137L, "Kalngale", 57.1000, 24.3000);
        createStation(stationMap, stations, 138L, "Garciems", 57.1200, 24.3200);
        createStation(stationMap, stations, 139L, "Garupe", 57.1400, 24.3400);
        createStation(stationMap, stations, 140L, "Carnikava", 57.1310, 24.2140);
        createStation(stationMap, stations, 141L, "Gauja", 57.1600, 24.3800);
        createStation(stationMap, stations, 142L, "Lilaste", 57.1800, 24.4000);
        createStation(stationMap, stations, 143L, "Incupe", 57.2000, 24.4200);
        createStation(stationMap, stations, 144L, "Pabazi", 57.2200, 24.4400);
        createStation(stationMap, stations, 145L, "Saulkrasti", 57.2650, 24.4130);
        createStation(stationMap, stations, 146L, "Kisupe", 57.2900, 24.4500);
        createStation(stationMap, stations, 147L, "Zvejniekciems", 57.3100, 24.4700);
        createStation(stationMap, stations, 148L, "Skulte", 57.3300, 24.5000);

        // Second pass: set up neighbor relationships (simplified - based on routes)
        // Neighbors will be set implicitly by routes for this demo

        return stations;
    }

    private static void createStation(Map<Long, Station> stationMap, List<Station> stations,
            Long id, String name, double lat, double lon) {
        Station station = new Station(id, name, lat, lon, new ArrayList<>());
        stationMap.put(id, station);
        stations.add(station);
    }

    /**
     * Generate routes covering all major lines, including reverse routes.
     */
    private static List<Route> generateRoutes(Map<Long, Station> stationMap) {
        List<Route> routes = new ArrayList<>();
        AtomicLong nextRouteId = new AtomicLong(1L);

        // Helper function to create a list of stations from IDs
        BiFunction<Map<Long, Station>, Long[], List<Station>> getStationsFromIds = (sMap, ids) -> {
            List<Station> routeStations = new ArrayList<>();
            for (Long stationId : ids) {
                Station station = sMap.get(stationId);
                if (station != null) {
                    routeStations.add(station);
                }
            }
            return routeStations;
        };

        // Helper function to add a route and its reverse
        BiConsumer<String, Long[]> addRouteAndReverse = (namePrefix, stationIds) -> {
            // Forward route
            List<Station> forwardStations = getStationsFromIds.apply(stationMap, stationIds);
            routes.add(new Route(nextRouteId.getAndIncrement(),
                    namePrefix + " (" + forwardStations.get(0).getName() + "-" + forwardStations.get(forwardStations.size() - 1).getName() + ")",
                    forwardStations));

            // Reverse route
            List<Station> reverseStations = new ArrayList<>(forwardStations);
            Collections.reverse(reverseStations);
            routes.add(new Route(nextRouteId.getAndIncrement(),
                    namePrefix + " (" + reverseStations.get(0).getName() + "-" + reverseStations.get(reverseStations.size() - 1).getName() + ")",
                    reverseStations));
        };

        // Define routes
        addRouteAndReverse.accept("Riga-Liepaja", new Long[]{
                1L, 3L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L, 72L, 73L, 74L, 75L, 76L});

        addRouteAndReverse.accept("Riga-Tukums II", new Long[]{
                1L, 3L, 87L, 88L, 89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L, 98L, 99L, 100L,
                101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L});

        addRouteAndReverse.accept("Riga-Valga", new Long[]{
                1L, 4L, 110L, 111L, 112L, 113L, 114L, 115L, 116L, 117L, 118L, 119L, 120L, 121L,
                122L, 123L, 124L, 125L, 126L, 127L, 128L, 129L});

        addRouteAndReverse.accept("Riga-Skulte", new Long[]{
                1L, 4L, 130L, 131L, 132L, 133L, 134L, 135L, 136L, 137L, 138L, 139L, 140L, 141L,
                142L, 143L, 144L, 145L, 146L, 147L, 148L});

        addRouteAndReverse.accept("Riga-Ogre", new Long[]{
                1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L});

        addRouteAndReverse.accept("Riga-Indra", new Long[]{
                1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L});

        addRouteAndReverse.accept("Riga-Zilupe", new Long[]{
                1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 39L, 40L, 41L, 42L, 43L, 44L, 45L,
                46L, 47L, 48L, 49L, 50L, 51L, 52L, 53L});

        addRouteAndReverse.accept("Riga-Gulbene", new Long[]{
                1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 54L, 55L, 56L, 57L, 58L, 59L, 60L});

        addRouteAndReverse.accept("Riga-Jelgava", new Long[]{
                1L, 3L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L});

        addRouteAndReverse.accept("Riga-Jurmala", new Long[]{
                1L, 3L, 87L, 88L, 89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L});

        return routes;
    }

    /**
     * Generate trains with varying capacities
     */
    private static List<Train> generateTrains(int count) {
        List<Train> trains = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int capacity;
            if (i < count / 3) {
                capacity = 100 + (i * 10); // Small: 100, 110, 120...
            } else if (i < 2 * count / 3) {
                capacity = 200 + ((i - count / 3) * 10); // Medium: 200, 210, 220...
            } else {
                capacity = 280 + ((i - 2 * count / 3) * 10); // Large: 280, 290, 300...
            }
            trains.add(new Train((long) (i + 1), capacity));
        }

        return trains;
    }

    /**
     * Generate depots at major stations and terminal stations
     * Trains must end their day at a depot, so all route terminals need depots
     */
    private static List<Depo> generateDepos(Map<Long, Station> stationMap) {
        List<Depo> depos = new ArrayList<>();

        // Main depots at major stations (these are larger hubs)
        depos.add(new Depo(1L, stationMap.get(1L), 20));   // Riga - main hub
        depos.add(new Depo(2L, stationMap.get(2L), 15));   // Vagonu parks - main depot

        // Terminal depots at end of each route (smaller capacity)
        depos.add(new Depo(3L, stationMap.get(76L), 3));   // Liepaja (route 1 end)
        depos.add(new Depo(4L, stationMap.get(109L), 3));  // Tukums II (route 2 end)
        depos.add(new Depo(5L, stationMap.get(129L), 3));  // Valga (route 3 end)
        depos.add(new Depo(6L, stationMap.get(148L), 3));  // Skulte (route 4 end)
        depos.add(new Depo(7L, stationMap.get(16L), 5));   // Ogre (route 5 end)
        depos.add(new Depo(8L, stationMap.get(38L), 3));   // Indra (route 6 end)
        depos.add(new Depo(9L, stationMap.get(53L), 3));   // Zilupe (route 7 end)
        depos.add(new Depo(10L, stationMap.get(60L), 3));  // Gulbene (route 8 end)
        depos.add(new Depo(11L, stationMap.get(71L), 5));  // Jelgava (route 9 end)
        depos.add(new Depo(12L, stationMap.get(97L), 3));  // Dubulti (route 10 end)

        return depos;
    }

    /**
     * Generate train-depo assignments
     */
    private static List<TrainDepoAssignment> generateTrainDepoAssignments(List<Train> trains, List<Depo> depos) {
        List<TrainDepoAssignment> assignments = new ArrayList<>();

        for (int i = 0; i < trains.size(); i++) {
            Train train = trains.get(i);
            // Distribute trains across depots
            Depo depo = depos.get(i % depos.size());
            assignments.add(new TrainDepoAssignment((long) (i + 1), train, depo));
        }

        return assignments;
    }

    /**
     * Generate hourly passenger demand (6 AM to 10 PM)
     */
    private static List<StationDemand> generateStationDemands(List<Station> stations, List<Route> routes) {
        List<StationDemand> demands = new ArrayList<>();
        long id = 1L;

        for (Route route : routes) {
            for (Station station : route.getStations()) {
                // Generate hourly demand from 6 AM to 10 PM
                for (int hour = 6; hour <= 22; hour++) {
                    int[] passengerCounts = generateDemand(route.getId(), hour);
                    demands.add(new StationDemand(
                            id++,
                            station,
                            route,
                            LocalTime.of(hour, 0),
                            passengerCounts[0],  // embarking
                            passengerCounts[1]   // disembarking
                    ));
                }
            }
        }

        return demands;
    }

    /**
     * Generate realistic passenger demand based on route and time
     * Returns [embarking, disembarking] counts
     */
    private static int[] generateDemand(Long routeId, int hour) {
        int embarking, disembarking;

        // Morning rush hour (7-9 AM) - more people embarking
        if (hour >= 7 && hour <= 9) {
            embarking = 50 + RANDOM.nextInt(100);
            disembarking = 20 + RANDOM.nextInt(40);
        }
        // Evening rush hour (5-7 PM) - more people disembarking
        else if (hour >= 17 && hour <= 19) {
            embarking = 20 + RANDOM.nextInt(40);
            disembarking = 50 + RANDOM.nextInt(100);
        }
        // Mid-day - balanced
        else if (hour >= 10 && hour <= 16) {
            embarking = 20 + RANDOM.nextInt(40);
            disembarking = 20 + RANDOM.nextInt(40);
        }
        // Early morning / late evening - low traffic
        else {
            embarking = 10 + RANDOM.nextInt(20);
            disembarking = 10 + RANDOM.nextInt(20);
        }

        return new int[]{embarking, disembarking};
    }

    /**
     * Generate departure times (planning entities)
     *
     * Each DepartureTime represents a TRAIN TRIP - a train starting at the first station
     * of a route and covering the entire route. Timefold assigns which train runs the trip
     * and at what time it departs.
     */
    private static List<DepartureTime> generateDepartureTimes(List<Route> routes,
            List<StationDemand> stationDemands, int startHour, int endHour, int intervalHours) {
        List<DepartureTime> departureTimes = new ArrayList<>();
        long id = 1L;

        // Build lookup: (stationId, routeId) -> (hour -> StationDemand)
        Map<String, Map<Integer, StationDemand>> demandLookup = new HashMap<>();
        for (StationDemand demand : stationDemands) {
            String key = demand.getStation().getId() + "-" + demand.getRoute().getId();
            demandLookup.computeIfAbsent(key, k -> new HashMap<>())
                    .put(demand.getTime().getHour(), demand);
        }

        for (Route route : routes) {
            Station firstStation = route.getStations().get(0);
            String lookupKey = firstStation.getId() + "-" + route.getId();
            Map<Integer, StationDemand> hourlyDemands = demandLookup.getOrDefault(lookupKey, new HashMap<>());

            // Generate one departure per time slot per route
            // This represents "we need a train to run this route at approximately this hour"
            for (int hour = startHour; hour <= endHour; hour += intervalHours) {
                DepartureTime departure = new DepartureTime();
                departure.setId(id++);
                departure.setStation(firstStation);
                departure.setRoute(route);
                departure.setStationIndexInRoute(0);
                departure.setHourlyDemands(hourlyDemands);
                // Train and departure time are null - Timefold will assign them
                departureTimes.add(departure);
            }
        }

        return departureTimes;
    }

    /**
     * Get time offset for route to prevent conflicts
     */
    private static int getRouteOffset(Long routeId) {
        switch (routeId.intValue()) {
            case 1: return 0;   // Riga-Liepaja: :00
            case 2: return 10;  // Riga-Tukums II: :10
            case 3: return 20;  // Riga-Valga: :20
            case 4: return 30;  // Riga-Skulte: :30
            case 5: return 40;  // Riga-Ogre: :40
            case 6: return 5;   // Riga-Indra: :05
            case 7: return 15;  // Riga-Zilupe: :15
            case 8: return 25;  // Riga-Gulbene: :25
            case 9: return 35;  // Riga-Jelgava: :35
            case 10: return 45; // Riga-Jurmala: :45
            default: return 0;
        }
    }
}
