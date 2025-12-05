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
 * Creates:
 * - 5-10 stations with coordinates
 * - 3-5 routes connecting stations
 * - 5-8 trains with different capacities
 * - 2-3 depots
 * - Hourly passenger demand
 * - Unassigned departure times (planning entities)
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
     * Generates default size dataset (99 departures)
     */
    public static RollingStockSchedule generateDefaultDataset() {
        return generateDataset(6, 22, 2); // 6 AM to 10 PM, every 2 hours
    }
    
    /**
     * Generates small dataset for quick testing (50 departures)
     */
    public static RollingStockSchedule generateSmallDataset() {
        return generateDataset(8, 18, 2); // 8 AM to 6 PM, every 2 hours
    }
    
    /**
     * Generates large dataset for stress testing (200 departures)
     */
    public static RollingStockSchedule generateLargeDataset() {
        return generateDataset(6, 22, 1); // 6 AM to 10 PM, every hour
    }
    
    /**
     * Generates dataset with configurable time range
     */
    private static RollingStockSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        // Create configuration
        Konfiguracija konfiguracija = new Konfiguracija(
                Duration.ofMinutes(5), // Minimum 5 minutes between trains
                Duration.ofMinutes(2)  // 2 minutes station stop
        );
        
        // Create stations
        List<Stacija> stacijas = generateStacijas();
        
        // Create routes
        List<Marsruts> marsruti = generateMarsruti(stacijas);
        
        // Create trains (more trains for larger datasets)
        List<Vilciens> vilcieni = generateVilcieni(intervalHours == 1 ? 12 : 7);
        
        // Create depots
        List<Depo> depo = generateDepo(vilcieni, stacijas);
        
        // Create passenger demand
        List<CilvekuPieprasijums> cilvekuPieprasijumi = generateCilvekuPieprasijums(
                stacijas, marsruti
        );
        
        // Create unassigned departure times (planning entities)
        List<AtiesanasLaiks> atiesanasLaiki = generateAtiesanasLaiki(
                stacijas, marsruti, cilvekuPieprasijumi, startHour, endHour, intervalHours
        );
        
        return new RollingStockSchedule(
                vilcieni,
                stacijas,
                marsruti,
                depo,
                cilvekuPieprasijumi,
                konfiguracija,
                atiesanasLaiki
        );
    }
    
    /**
     * Generate 8 stations in Latvia
     */
    private static List<Stacija> generateStacijas() {
        List<Stacija> stacijas = new ArrayList<>();
        
        // Major Latvian cities with approximate coordinates
        stacijas.add(createStacija(1L, "Rīga", 56.9496, 24.1052, Arrays.asList(2L, 3L, 4L)));
        stacijas.add(createStacija(2L, "Jelgava", 56.6505, 23.7226, Arrays.asList(1L, 5L)));
        stacijas.add(createStacija(3L, "Jūrmala", 56.9682, 23.7746, Arrays.asList(1L)));
        stacijas.add(createStacija(4L, "Valmiera", 57.5383, 25.4266, Arrays.asList(1L, 6L)));
        stacijas.add(createStacija(5L, "Liepāja", 56.5046, 21.0114, Arrays.asList(2L)));
        stacijas.add(createStacija(6L, "Cēsis", 57.3119, 25.2673, Arrays.asList(4L, 7L)));
        stacijas.add(createStacija(7L, "Sigulda", 57.1536, 24.8536, Arrays.asList(6L, 1L)));
        stacijas.add(createStacija(8L, "Ogre", 56.8162, 24.6041, Arrays.asList(1L)));
        
        return stacijas;
    }
    
    private static Stacija createStacija(Long id, String nosaukums, double lat, double lon, 
                                   List<Long> kaimini) {
        Stacija stacija = new Stacija(id, nosaukums, new GeoCoordinates(lat, lon));
        stacija.setKaiminiStacijas(kaimini);
        return stacija;
    }
    
    /**
     * Generate 4 routes
     */
    private static List<Marsruts> generateMarsruti(List<Stacija> stacijas) {
        List<Marsruts> marsruti = new ArrayList<>();
        
        // Route 1: Rīga -> Jelgava -> Liepāja (long distance)
        marsruti.add(new Marsruts(1L, "Rīga-Liepāja", Arrays.asList(1L, 2L, 5L)));
        
        // Route 2: Rīga -> Jūrmala (short distance, high frequency)
        marsruti.add(new Marsruts(2L, "Rīga-Jūrmala", Arrays.asList(1L, 3L)));
        
        // Route 3: Rīga -> Sigulda -> Cēsis -> Valmiera (north route)
        marsruti.add(new Marsruts(3L, "Rīga-Valmiera", Arrays.asList(1L, 7L, 6L, 4L)));
        
        // Route 4: Rīga -> Ogre (commuter route)
        marsruti.add(new Marsruts(4L, "Rīga-Ogre", Arrays.asList(1L, 8L)));
        
        return marsruti;
    }
    
    /**
     * Generate trains with varying capacities
     */
    private static List<Vilciens> generateVilcieni(int count) {
        List<Vilciens> vilcieni = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int capacity;
            if (i < count / 3) {
                capacity = 100 + (i * 10); // Small: 100, 110, 120...
            } else if (i < 2 * count / 3) {
                capacity = 200 + ((i - count/3) * 10); // Medium: 200, 210, 220...
            } else {
                capacity = 280 + ((i - 2*count/3) * 10); // Large: 280, 290, 300...
            }
            vilcieni.add(new Vilciens((long)(i + 1), capacity));
        }
        
        return vilcieni;
    }
    
    /**
     * Generate depots for all trains
     */
    private static List<Depo> generateDepo(List<Vilciens> vilcieni, List<Stacija> stacijas) {
        List<Depo> depo = new ArrayList<>();
        
        // Create depots for all trains (most in Rīga)
        for (int i = 0; i < vilcieni.size(); i++) {
            Long vilciensId = vilcieni.get(i).getId();
            Long stacijaId = (i % 3 == 0) ? 2L : 1L; // Most in Rīga (1L), some in Jelgava (2L)
            depo.add(new Depo((long)(i + 1), vilciensId, stacijaId));
        }
        
        return depo;
    }
    
    /**
     * Generate hourly passenger demand (6 AM to 10 PM)
     */
    private static List<CilvekuPieprasijums> generateCilvekuPieprasijums(
            List<Stacija> stacijas, List<Marsruts> marsruti) {
        List<CilvekuPieprasijums> pieprasijumi = new ArrayList<>();
        long id = 1L;
        
        // Generate demand for each route and station
        for (Marsruts marsruts : marsruti) {
            for (Long stacijaId : marsruts.getStacijas()) {
                // Generate hourly demand from 6 AM to 10 PM
                for (int hour = 6; hour <= 22; hour++) {
                    int demand = generateDemand(marsruts.getId(), hour);
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
     * Generate realistic passenger demand based on route and time
     */
    private static int generateDemand(Long marsrutaId, int hour) {
        // Morning rush hour (7-9 AM)
        if (hour >= 7 && hour <= 9) {
            return 50 + RANDOM.nextInt(100); // 50-150 passengers
        }
        // Evening rush hour (5-7 PM)
        else if (hour >= 17 && hour <= 19) {
            return 40 + RANDOM.nextInt(80); // 40-120 passengers
        }
        // Mid-day
        else if (hour >= 10 && hour <= 16) {
            return 20 + RANDOM.nextInt(40); // 20-60 passengers
        }
        // Early morning / late evening
        else {
            return 10 + RANDOM.nextInt(20); // 10-30 passengers
        }
    }
    
    /**
     * Generate unassigned departure times (planning entities)
     * 
     * Creates departures every 30-60 minutes for each route
     */
    private static List<AtiesanasLaiks> generateAtiesanasLaiki(
            List<Stacija> stacijas, 
            List<Marsruts> marsruti,
            List<CilvekuPieprasijums> cilvekuPieprasijumi,
            int startHour,
            int endHour,
            int intervalHours) {
        List<AtiesanasLaiks> atiesanasLaiki = new ArrayList<>();
        long id = 1L;
        
        // Generate departures for each route with time offsets to avoid conflicts
        for (Marsruts marsruts : marsruti) {
            int offsetMinutes = getRouteOffset(marsruts.getId());
            
            // Generate departures with specified time range and interval
            for (int hour = startHour; hour <= endHour; hour += intervalHours) {
                LocalTime startTime = LocalTime.of(hour, offsetMinutes);
                
                // Create departure for each station on the route with progressive times
                int stationIndex = 0;
                for (Long stacijaId : marsruts.getStacijas()) {
                    // Add travel time between stations (30 minutes per station hop)
                    LocalTime stationTime = startTime.plusMinutes(stationIndex * 30L);
                    
                    int cilvekuDelta = getExpectedPassengers(
                            stacijaId, marsruts.getId(), stationTime, cilvekuPieprasijumi
                    );
                    
                    atiesanasLaiki.add(new AtiesanasLaiks(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            stationTime,
                            cilvekuDelta
                    ));
                    // vilciensId is null (unassigned) - Timefold will assign it
                    
                    stationIndex++;
                }
            }
        }
        
        return atiesanasLaiki;
    }
    
    /**
     * Generate unassigned departure times (planning entities) - Legacy method
     * 
     * Creates departures every 30-60 minutes for each route
     */
    private List<AtiesanasLaiks> generateAtiesanasLaiki(
            List<Stacija> stacijas, 
            List<Marsruts> marsruti,
            List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        List<AtiesanasLaiks> atiesanasLaiki = new ArrayList<>();
        long id = 1L;
        
        // Generate departures for each route with time offsets to avoid conflicts
        for (Marsruts marsruts : marsruti) {
            int offsetMinutes = getRouteOffset(marsruts.getId());
            
            // Generate departures from 6 AM to 10 PM - every 2 hours
            for (int hour = 6; hour <= 22; hour += 2) {
                LocalTime laiks = LocalTime.of(hour, offsetMinutes);
                
                // Create departure for each station on the route
                for (Long stacijaId : marsruts.getStacijas()) {
                    int cilvekuDelta = getExpectedPassengers(
                            stacijaId, marsruts.getId(), laiks, cilvekuPieprasijumi
                    );
                    
                    atiesanasLaiki.add(new AtiesanasLaiks(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            laiks,
                            cilvekuDelta
                    ));
                    // vilciensId is null (unassigned) - Timefold will assign it
                }
            }
        }
        
        return atiesanasLaiki;
    }
    
    /**
     * Get time offset for route to prevent conflicts
     * Each route starts at different minute to avoid simultaneous departures
     */
    private static int getRouteOffset(Long marsrutaId) {
        switch (marsrutaId.intValue()) {
            case 1: return 0;   // Rīga-Liepāja: 06:00, 08:00, 10:00...
            case 2: return 15;  // Rīga-Jūrmala: 06:15, 08:15, 10:15...
            case 3: return 30;  // Rīga-Valmiera: 06:30, 08:30, 10:30...
            case 4: return 45;  // Rīga-Ogre: 06:45, 08:45, 10:45...
            default: return 0;
        }
    }
    
    /**
     * Get expected passengers for a departure based on demand
     */
    private static int getExpectedPassengers(Long stacijaId, Long marsrutaId, LocalTime laiks,
                                       List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        // Find matching demand (simplified: use exact hour match)
        return cilvekuPieprasijumi.stream()
                .filter(p -> p.getStacijasId().equals(stacijaId))
                .filter(p -> p.getMarsrutaId().equals(marsrutaId))
                .filter(p -> p.getStunda().getHour() == laiks.getHour())
                .mapToInt(CilvekuPieprasijums::getCilvekuSkaits)
                .findFirst()
                .orElse(0);
    }
}
