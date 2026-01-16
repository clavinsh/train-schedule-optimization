package org.acme.rollingstockrostering.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.rollingstockrostering.domain.*;

/**
 * DemoDataGenerator - Generates test data for the Rolling Stock Rostering problem
 *
 * Creates a complete dataset with:
 * - Stations with coordinates (Latvian railway network)
 * - Routes connecting stations
 * - Trains with different capacities
 * - Depots at major stations
 * - Train-depot assignments
 * - Passenger demand by station, route, and time
 * - Trips to be optimized (planning entities)
 */
@ApplicationScoped
public class DemoDataGenerator {

    private static final Random RANDOM = new Random(37); // Fixed seed for reproducibility

    /**
     * Generates a complete RollingStockSchedule with demo data.
     */
    public RollingStockSchedule generateDemoData() {
        return generateDefaultDataset();
    }

    /**
     * Generates default size dataset (~80 trips).
     */
    public static RollingStockSchedule generateDefaultDataset() {
        return generateDataset(6, 22, 2); // 6 AM to 10 PM, every 2 hours
    }

    /**
     * Generates small dataset for quick testing (~40 trips).
     */
    public static RollingStockSchedule generateSmallDataset() {
        return generateDataset(8, 18, 2); // 8 AM to 6 PM, every 2 hours
    }

    /**
     * Generates large dataset for stress testing (~160 trips).
     */
    public static RollingStockSchedule generateLargeDataset() {
        return generateDataset(6, 22, 1); // 6 AM to 10 PM, every hour
    }

    /**
     * Generates dataset with configurable time range.
     */
    private static RollingStockSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        // Create configuration
        Konfiguracija konfiguracija = new Konfiguracija(
                Duration.ofMinutes(5),  // Minimum 5 minutes between trains
                Duration.ofMinutes(2)   // 2 minutes station stop
        );

        // Create stations
        List<Stacija> stacijas = generateStacijas();

        // Create routes
        List<Marsruts> marsruti = generateMarsruti();

        // Create trains (more trains for larger datasets)
        int trainCount = intervalHours == 1 ? 40 : 30;
        List<Vilciens> vilcieni = generateVilcieni(trainCount);

        // Create depots (physical locations)
        List<Depo> depiList = generateDepos();

        // Create train-depot assignments
        List<TrainDepotAssignment> trainDepotAssignments = generateTrainDepotAssignments(vilcieni, depiList);

        // Create passenger demand
        List<CilvekuPieprasijums> cilvekuPieprasijumi = generateCilvekuPieprasijums(marsruti);

        // Create trips (planning entities)
        List<Trip> trips = generateTrips(marsruti, startHour, endHour, intervalHours);

        return new RollingStockSchedule(
                vilcieni,
                stacijas,
                marsruti,
                depiList,
                trainDepotAssignments,
                cilvekuPieprasijumi,
                konfiguracija,
                trips
        );
    }

    /**
     * Generate stations in Latvia.
     */
    private static List<Stacija> generateStacijas() {
        List<Stacija> stacijas = new ArrayList<>();

        // Major hub stations
        stacijas.add(createStacija(1L, "Riga", 56.9496, 24.1052, Arrays.asList(2L, 3L, 4L)));
        stacijas.add(createStacija(2L, "Vagonu parks", 56.9350, 24.1200, Arrays.asList(1L, 5L)));
        stacijas.add(createStacija(3L, "Tornakalns", 56.9350, 24.0800, Arrays.asList(1L, 61L, 87L)));
        stacijas.add(createStacija(4L, "Zemitani", 56.9600, 24.1300, Arrays.asList(1L, 110L, 130L)));

        // Eastern line stations (Riga - Ogre - Krustpils)
        stacijas.add(createStacija(5L, "Janavārti", 56.9280, 24.1350, Arrays.asList(2L, 6L)));
        stacijas.add(createStacija(6L, "Daugmale", 56.9200, 24.1500, Arrays.asList(5L, 7L)));
        stacijas.add(createStacija(7L, "Skirotava", 56.9100, 24.1650, Arrays.asList(6L, 8L)));
        stacijas.add(createStacija(8L, "Gaisma", 56.9000, 24.1800, Arrays.asList(7L, 9L)));
        stacijas.add(createStacija(9L, "Rumbula", 56.8900, 24.1950, Arrays.asList(8L, 10L)));
        stacijas.add(createStacija(10L, "Darzini", 56.8800, 24.2100, Arrays.asList(9L, 11L)));
        stacijas.add(createStacija(11L, "Dole", 56.8700, 24.2250, Arrays.asList(10L, 12L)));
        stacijas.add(createStacija(12L, "Salaspils", 56.8600, 24.2400, Arrays.asList(11L, 13L)));
        stacijas.add(createStacija(13L, "Saulkalne", 56.8500, 24.2550, Arrays.asList(12L, 14L)));
        stacijas.add(createStacija(14L, "Ikskile", 56.8400, 24.2700, Arrays.asList(13L, 15L)));
        stacijas.add(createStacija(15L, "Jaunogre", 56.8300, 24.2850, Arrays.asList(14L, 16L)));
        stacijas.add(createStacija(16L, "Ogre", 56.8200, 24.6000, Arrays.asList(15L)));

        // Western line stations (Riga - Jelgava)
        stacijas.add(createStacija(61L, "Bierini", 56.9200, 24.0500, Arrays.asList(3L, 62L)));
        stacijas.add(createStacija(62L, "BA Turiba", 56.9100, 24.0300, Arrays.asList(61L, 63L)));
        stacijas.add(createStacija(63L, "Tiraine", 56.9000, 24.0100, Arrays.asList(62L, 64L)));
        stacijas.add(createStacija(64L, "Medemciems", 56.8800, 23.9800, Arrays.asList(63L, 65L)));
        stacijas.add(createStacija(65L, "Jaunolaine", 56.8500, 23.9500, Arrays.asList(64L, 66L)));
        stacijas.add(createStacija(66L, "Olaine", 56.7950, 23.9390, Arrays.asList(65L, 67L)));
        stacijas.add(createStacija(67L, "Dalbe", 56.7500, 23.9000, Arrays.asList(66L, 68L)));
        stacijas.add(createStacija(68L, "Cena", 56.7000, 23.8500, Arrays.asList(67L, 69L)));
        stacijas.add(createStacija(69L, "Ozolnieki", 56.6880, 23.7820, Arrays.asList(68L, 70L)));
        stacijas.add(createStacija(70L, "Cukurfabrika", 56.6500, 23.7300, Arrays.asList(69L, 71L)));
        stacijas.add(createStacija(71L, "Jelgava", 56.6530, 23.7130, Arrays.asList(70L)));

        // Jurmala line stations (Riga - Tukums)
        stacijas.add(createStacija(87L, "Zasulauks", 56.9300, 24.0600, Arrays.asList(3L, 88L)));
        stacijas.add(createStacija(88L, "Depo", 56.9250, 24.0400, Arrays.asList(87L, 89L)));
        stacijas.add(createStacija(89L, "Zolitūde", 56.9380, 24.0520, Arrays.asList(88L, 90L)));
        stacijas.add(createStacija(90L, "Imanta", 56.9490, 24.0210, Arrays.asList(89L, 91L)));
        stacijas.add(createStacija(91L, "Babite", 56.9550, 23.9540, Arrays.asList(90L, 92L)));
        stacijas.add(createStacija(92L, "Priedaine", 56.9600, 23.9200, Arrays.asList(91L, 93L)));
        stacijas.add(createStacija(93L, "Lielupe", 56.9680, 23.8200, Arrays.asList(92L, 94L)));
        stacijas.add(createStacija(94L, "Bulduri", 56.9700, 23.7750, Arrays.asList(93L, 95L)));
        stacijas.add(createStacija(95L, "Dzintari", 56.9730, 23.7630, Arrays.asList(94L, 96L)));
        stacijas.add(createStacija(96L, "Majori", 56.9750, 23.7500, Arrays.asList(95L, 97L)));
        stacijas.add(createStacija(97L, "Dubulti", 56.9800, 23.7300, Arrays.asList(96L)));

        // Northern line stations (Riga - Sigulda)
        stacijas.add(createStacija(110L, "Ciekurkalns", 56.9700, 24.1500, Arrays.asList(4L, 111L)));
        stacijas.add(createStacija(111L, "Smerlis", 56.9850, 24.1700, Arrays.asList(110L, 112L)));
        stacijas.add(createStacija(112L, "Jugla", 57.0280, 24.3260, Arrays.asList(111L, 113L)));
        stacijas.add(createStacija(113L, "Garkalne", 57.0500, 24.4000, Arrays.asList(112L, 114L)));
        stacijas.add(createStacija(114L, "Krievupe", 57.0800, 24.5000, Arrays.asList(113L, 115L)));
        stacijas.add(createStacija(115L, "Vangazi", 57.0900, 24.5340, Arrays.asList(114L, 116L)));
        stacijas.add(createStacija(116L, "Incukalns", 57.1000, 24.6860, Arrays.asList(115L, 117L)));
        stacijas.add(createStacija(117L, "Eglupe", 57.1200, 24.7500, Arrays.asList(116L, 118L)));
        stacijas.add(createStacija(118L, "Sigulda", 57.1540, 24.8530, Arrays.asList(117L)));

        // Coastal line stations (Riga - Skulte)
        stacijas.add(createStacija(130L, "Brasa", 56.9800, 24.1600, Arrays.asList(4L, 131L)));
        stacijas.add(createStacija(131L, "Sarkandaugava", 57.0000, 24.1800, Arrays.asList(130L, 132L)));
        stacijas.add(createStacija(132L, "Dauderi", 57.0200, 24.2000, Arrays.asList(131L, 133L)));
        stacijas.add(createStacija(133L, "Mangali", 57.0400, 24.2200, Arrays.asList(132L, 134L)));
        stacijas.add(createStacija(134L, "Ziemelblazma", 57.0600, 24.2400, Arrays.asList(133L, 135L)));
        stacijas.add(createStacija(135L, "Vecdaugava", 57.0800, 24.2600, Arrays.asList(134L, 136L)));
        stacijas.add(createStacija(136L, "Vecaki", 57.0900, 24.2800, Arrays.asList(135L, 137L)));
        stacijas.add(createStacija(137L, "Kalngale", 57.1000, 24.3000, Arrays.asList(136L, 138L)));
        stacijas.add(createStacija(138L, "Garciems", 57.1200, 24.3200, Arrays.asList(137L, 139L)));
        stacijas.add(createStacija(139L, "Carnikava", 57.1310, 24.2140, Arrays.asList(138L, 140L)));
        stacijas.add(createStacija(140L, "Saulkrasti", 57.2650, 24.4130, Arrays.asList(139L)));

        return stacijas;
    }

    private static Stacija createStacija(Long id, String nosaukums, double lat, double lon, List<Long> kaimini) {
        Stacija stacija = new Stacija(id, nosaukums, new GeoCoordinates(lat, lon));
        stacija.setKaiminiStacijas(kaimini);
        return stacija;
    }

    /**
     * Generate routes covering major lines.
     */
    private static List<Marsruts> generateMarsruti() {
        List<Marsruts> marsruti = new ArrayList<>();

        // Route 1: Riga -> Ogre (commuter route, high frequency)
        marsruti.add(new Marsruts(1L, "Riga-Ogre", Arrays.asList(
                1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L
        )));

        // Route 2: Riga -> Jelgava (commuter route)
        marsruti.add(new Marsruts(2L, "Riga-Jelgava", Arrays.asList(
                1L, 3L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L
        )));

        // Route 3: Riga -> Jurmala/Dubulti (coastal commuter, very high frequency)
        marsruti.add(new Marsruts(3L, "Riga-Jurmala", Arrays.asList(
                1L, 3L, 87L, 88L, 89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L
        )));

        // Route 4: Riga -> Sigulda (northern route)
        marsruti.add(new Marsruts(4L, "Riga-Sigulda", Arrays.asList(
                1L, 4L, 110L, 111L, 112L, 113L, 114L, 115L, 116L, 117L, 118L
        )));

        // Route 5: Riga -> Saulkrasti (coastal route)
        marsruti.add(new Marsruts(5L, "Riga-Saulkrasti", Arrays.asList(
                1L, 4L, 130L, 131L, 132L, 133L, 134L, 135L, 136L, 137L, 138L, 139L, 140L
        )));

        return marsruti;
    }

    /**
     * Generate trains with varying capacities.
     */
    private static List<Vilciens> generateVilcieni(int count) {
        List<Vilciens> vilcieni = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int capacity;
            String type;
            if (i < count / 3) {
                capacity = 100 + (i * 10); // Small: 100-190
                type = "ER2";
            } else if (i < 2 * count / 3) {
                capacity = 200 + ((i - count / 3) * 10); // Medium: 200-290
                type = "ER2T";
            } else {
                capacity = 280 + ((i - 2 * count / 3) * 10); // Large: 280-370
                type = "ER2M";
            }
            vilcieni.add(new Vilciens((long) (i + 1), capacity, type + "-" + (i + 1)));
        }

        return vilcieni;
    }

    /**
     * Generate depot locations at major stations.
     */
    private static List<Depo> generateDepos() {
        List<Depo> depiList = new ArrayList<>();

        // Main depot at Riga central
        depiList.add(new Depo(1L, "Riga Centrala Depo", 1L, 50));

        // Depot at Vagonu Parks (main maintenance facility)
        depiList.add(new Depo(2L, "Vagonu Parks Depo", 2L, 30));

        // Depot at Jelgava (western terminal)
        depiList.add(new Depo(3L, "Jelgava Depo", 71L, 15));

        // Depot at Ogre (eastern terminal)
        depiList.add(new Depo(4L, "Ogre Depo", 16L, 10));

        // Depot on Jurmala line
        depiList.add(new Depo(5L, "Jurmala Depo", 88L, 20));

        return depiList;
    }

    /**
     * Generate train-to-depot assignments.
     * Distributes trains across available depots.
     */
    private static List<TrainDepotAssignment> generateTrainDepotAssignments(
            List<Vilciens> vilcieni, List<Depo> depiList) {
        List<TrainDepotAssignment> assignments = new ArrayList<>();

        for (int i = 0; i < vilcieni.size(); i++) {
            Long vilciensId = vilcieni.get(i).getId();

            // Distribute trains across depots in round-robin fashion
            int depoIndex = i % depiList.size();
            Long depoId = depiList.get(depoIndex).getId();

            assignments.add(new TrainDepotAssignment((long) (i + 1), vilciensId, depoId));
        }

        return assignments;
    }

    /**
     * Generate passenger demand for each route and hour.
     */
    private static List<CilvekuPieprasijums> generateCilvekuPieprasijums(List<Marsruts> marsruti) {
        List<CilvekuPieprasijums> pieprasijumi = new ArrayList<>();
        long id = 1L;

        for (Marsruts marsruts : marsruti) {
            // Generate demand for each station on the route
            for (int stationIndex = 0; stationIndex < marsruts.getStacijas().size(); stationIndex++) {
                Long stacijaId = marsruts.getStacijas().get(stationIndex);

                // Generate hourly demand from 6 AM to 10 PM
                for (int hour = 6; hour <= 22; hour++) {
                    int demand = generateDemand(marsruts.getId(), stationIndex, hour);
                    pieprasijumi.add(new CilvekuPieprasijums(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            LocalTime.of(hour, 0),
                            demand
                    ));
                }
            }
        }

        return pieprasijumi;
    }

    /**
     * Generate realistic passenger demand based on route, station position, and time.
     */
    private static int generateDemand(Long marsrutaId, int stationIndex, int hour) {
        // Base demand varies by time of day
        int baseDemand;
        if (hour >= 7 && hour <= 9) {
            // Morning rush hour
            baseDemand = 80 + RANDOM.nextInt(60); // 80-140
        } else if (hour >= 17 && hour <= 19) {
            // Evening rush hour
            baseDemand = 60 + RANDOM.nextInt(50); // 60-110
        } else if (hour >= 10 && hour <= 16) {
            // Mid-day
            baseDemand = 25 + RANDOM.nextInt(25); // 25-50
        } else {
            // Early morning / late evening
            baseDemand = 10 + RANDOM.nextInt(15); // 10-25
        }

        // Adjust by station position (higher at terminals and major hubs)
        if (stationIndex == 0) {
            // Origin station (terminal) - highest demand
            baseDemand = (int) (baseDemand * 1.5);
        } else if (stationIndex < 3) {
            // Near-origin stations - high demand
            baseDemand = (int) (baseDemand * 1.2);
        }

        // Adjust by route (some routes are busier)
        if (marsrutaId == 3L) {
            // Jurmala line is very popular
            baseDemand = (int) (baseDemand * 1.3);
        } else if (marsrutaId == 1L) {
            // Ogre line is popular for commuters
            baseDemand = (int) (baseDemand * 1.1);
        }

        return baseDemand;
    }

    /**
     * Generate trips (planning entities) for each route.
     * Each trip represents a complete journey along a route.
     */
    private static List<Trip> generateTrips(List<Marsruts> marsruti, int startHour, int endHour, int intervalHours) {
        List<Trip> trips = new ArrayList<>();
        long id = 1L;

        for (Marsruts marsruts : marsruti) {
            int offsetMinutes = getRouteOffset(marsruts.getId());

            // Generate outbound trips
            for (int hour = startHour; hour <= endHour; hour += intervalHours) {
                LocalTime startTime = LocalTime.of(hour, offsetMinutes);

                // Outbound trip
                trips.add(new Trip(id++, marsruts.getId(), startTime, false));

                // Return trip (starts after turnaround time)
                // Estimate trip duration based on station count
                int stationCount = marsruts.getStationCount();
                long tripDurationMinutes = (stationCount - 1) * 7; // ~7 min per station
                long turnaroundMinutes = 15;

                LocalTime returnStartTime = startTime.plusMinutes(tripDurationMinutes + turnaroundMinutes);
                if (returnStartTime.getHour() <= endHour) {
                    trips.add(new Trip(id++, marsruts.getId(), returnStartTime, true));
                }
            }
        }

        return trips;
    }

    /**
     * Get time offset for route to prevent conflicts at shared stations.
     * Each route starts at different minute to stagger departures.
     */
    private static int getRouteOffset(Long marsrutaId) {
        switch (marsrutaId.intValue()) {
            case 1: return 0;   // Riga-Ogre: 06:00, 08:00, 10:00...
            case 2: return 10;  // Riga-Jelgava: 06:10, 08:10, 10:10...
            case 3: return 20;  // Riga-Jurmala: 06:20, 08:20, 10:20...
            case 4: return 30;  // Riga-Sigulda: 06:30, 08:30, 10:30...
            case 5: return 40;  // Riga-Saulkrasti: 06:40, 08:40, 10:40...
            case 6: return 50;  // Additional routes...
            case 7: return 5;
            case 8: return 15;
            case 9: return 25;
            case 10: return 35;
            default: return (marsrutaId.intValue() * 7) % 60; // Spread remaining routes
        }
    }
}
