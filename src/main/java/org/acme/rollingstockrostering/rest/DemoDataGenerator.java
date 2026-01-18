package org.acme.rollingstockrostering.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.rollingstockrostering.domain.*;

/**
 * DemoDataGenerator - Generates test data for the Rolling Stock Rostering problem.
 * 
 * The key entity is Brauciens (Trip) - one complete journey on a route.
 * Each trip gets assigned to ONE train.
 */
@ApplicationScoped
public class DemoDataGenerator {
    
    private static final Random RANDOM = new Random(37);
    private static final int DEFAULT_SPEED_KMH = 60; // Average speed including stops
    private static final int STOP_TIME_MINUTES = 2;   // Time spent at each station
    
    public RollingStockSchedule generateDemoData() {
        return generateDefaultDataset();
    }
    
    public static RollingStockSchedule generateDefaultDataset() {
        // Reduced dataset to avoid OutOfMemoryError: 8-18 instead of 6-22, 3h intervals instead of 2h
        return generateDatasetWithParams(5, 8, 18, 3);
    }
    
    public RollingStockSchedule generateDataset(int trainCount, int startHour, int endHour, int intervalHours) {
        return generateDatasetWithParams(trainCount, startHour, endHour, intervalHours);
    }
    
    private static RollingStockSchedule generateDatasetWithParams(int trainCount, int startHour, int endHour, int intervalHours) {
        // Generate stations
        List<Stacija> stacijas = generateStacijas();
        Map<Long, Stacija> stacijuMapa = stacijas.stream()
                .collect(Collectors.toMap(Stacija::getId, s -> s));
        
        // Generate routes
        List<Marsruts> marsruti = generateMarsruti();
        
        // Generate trains - use parameter
        List<Vilciens> vilcieni = generateVilcieni(trainCount);
        
        // Generate depo (where trains are based)
        List<Depo> depo = generateDepo(vilcieni);
        
        // Generate passenger demand
        List<CilvekuPieprasijums> cilvekuPieprasijumi = generateCilvekuPieprasijums(stacijas, marsruti);
        
        // Configuration
        Konfiguracija konfiguracija = new Konfiguracija(
                Duration.ofMinutes(5),  // Min 5 min between trains at same station
                Duration.ofMinutes(2)   // 2 min stop at each station
        );
        
        // Generate trips (Braucieni) - this is the key planning entity
        List<Brauciens> braucieni = generateBraucieni(
                marsruti, stacijuMapa, cilvekuPieprasijumi,
                startHour, endHour, intervalHours
        );
        
        return new RollingStockSchedule(
                vilcieni, stacijas, marsruti, depo,
                cilvekuPieprasijumi, konfiguracija, braucieni
        );
    }
    
    private static List<Stacija> generateStacijas() {
        List<Stacija> stacijas = new ArrayList<>();
        
        // Rīga and surrounding stations with real coordinates
        stacijas.add(createStacija(1L, "Rīga Centrālā", 56.9496, 24.1052, Arrays.asList(2L, 3L, 4L)));
        stacijas.add(createStacija(2L, "Torņakalns", 56.9340, 24.0890, Arrays.asList(1L, 5L)));
        stacijas.add(createStacija(3L, "Zemitāni", 56.9680, 24.1350, Arrays.asList(1L, 6L)));
        stacijas.add(createStacija(4L, "Čiekurkalns", 56.9750, 24.1500, Arrays.asList(1L, 7L)));
        stacijas.add(createStacija(5L, "Olaine", 56.7850, 23.9380, Arrays.asList(2L, 8L)));
        stacijas.add(createStacija(6L, "Carnikava", 57.1290, 24.2780, Arrays.asList(3L, 9L)));
        stacijas.add(createStacija(7L, "Inčukalns", 57.0980, 24.6850, Arrays.asList(4L, 10L)));
        stacijas.add(createStacija(8L, "Jelgava", 56.6511, 23.7131, Arrays.asList(5L)));
        stacijas.add(createStacija(9L, "Saulkrasti", 57.2570, 24.4120, Arrays.asList(6L)));
        stacijas.add(createStacija(10L, "Sigulda", 57.1533, 24.8544, Arrays.asList(7L)));
        
        return stacijas;
    }
    
    private static Stacija createStacija(Long id, String nosaukums, double lat, double lon, List<Long> kaimini) {
        Stacija stacija = new Stacija(id, nosaukums, new GeoCoordinates(lat, lon));
        stacija.setKaiminiStacijas(kaimini);
        return stacija;
    }
    
    private static List<Marsruts> generateMarsruti() {
        List<Marsruts> marsruti = new ArrayList<>();
        // Three main routes from Rīga
        marsruti.add(new Marsruts(1L, "Rīga-Jelgava", Arrays.asList(1L, 2L, 5L, 8L)));
        marsruti.add(new Marsruts(2L, "Rīga-Saulkrasti", Arrays.asList(1L, 3L, 6L, 9L)));
        marsruti.add(new Marsruts(3L, "Rīga-Sigulda", Arrays.asList(1L, 4L, 7L, 10L)));
        return marsruti;
    }
    
    private static List<Vilciens> generateVilcieni(int count) {
        List<Vilciens> vilcieni = new ArrayList<>();
        // All trains are based in Rīga (station 1) since all routes start from there
        
        for (int i = 1; i <= count; i++) {
            // Capacity between 200 and 400
            int kapacitate = 200 + RANDOM.nextInt(200);
            // All trains have Rīga as their depo station (ID 1)
            vilcieni.add(new Vilciens((long) i, kapacitate, 1L));
        }
        return vilcieni;
    }
    
    private static List<Depo> generateDepo(List<Vilciens> vilcieni) {
        List<Depo> depo = new ArrayList<>();
        long id = 1;
        for (Vilciens vilciens : vilcieni) {
            // All trains start at Rīga Centrālā (station 1)
            depo.add(new Depo(id++, vilciens.getId(), vilciens.getDepoStacijaId()));
        }
        return depo;
    }
    
    private static List<CilvekuPieprasijums> generateCilvekuPieprasijums(
            List<Stacija> stacijas, List<Marsruts> marsruti) {
        List<CilvekuPieprasijums> pieprasijumi = new ArrayList<>();
        long id = 1;
        
        for (Marsruts marsruts : marsruti) {
            for (Long stacijaId : marsruts.getStacijas()) {
                for (int hour = 6; hour <= 22; hour++) {
                    int demand = generateDemand(hour);
                    pieprasijumi.add(new CilvekuPieprasijums(
                            id++, stacijaId, marsruts.getId(),
                            LocalTime.of(hour, 0), demand
                    ));
                }
            }
        }
        return pieprasijumi;
    }
    
    private static int generateDemand(int hour) {
        // Rush hour pattern - more passengers in morning and evening
        if (hour >= 7 && hour <= 9) return 150 + RANDOM.nextInt(100);   // Morning rush
        if (hour >= 16 && hour <= 18) return 120 + RANDOM.nextInt(80);  // Evening rush
        return 30 + RANDOM.nextInt(50);  // Off-peak
    }
    
    /**
     * Generate trips (Braucieni) for all routes.
     * Creates paired round-trip services: outbound and return trips are timed
     * so a train can complete one direction and immediately start the return.
     */
    private static List<Brauciens> generateBraucieni(
            List<Marsruts> marsruti,
            Map<Long, Stacija> stacijuMapa,
            List<CilvekuPieprasijums> cilvekuPieprasijumi,
            int startHour,
            int endHour,
            int intervalHours) {
        
        List<Brauciens> braucieni = new ArrayList<>();
        long id = 1;
        
        for (Marsruts marsruts : marsruti) {
            List<Long> stacijuIds = marsruts.getStacijas();
            Long firstStationId = stacijuIds.get(0);  // Rīga
            Long lastStationId = stacijuIds.get(stacijuIds.size() - 1);  // e.g. Jelgava
            
            // Calculate trip duration based on distance
            int tripDurationMinutes = calculateTripDuration(stacijuIds, stacijuMapa);
            int turnaroundMinutes = 10; // Time to prepare for return trip
            
            // Generate paired round-trips
            // Each "cycle" is: Rīga→Jelgava, wait, Jelgava→Rīga
            LocalTime currentTime = LocalTime.of(startHour, 0);
            
            while (currentTime.getHour() < endHour || 
                   (currentTime.getHour() == endHour && currentTime.getMinute() == 0)) {
                
                // OUTBOUND trip (Rīga → Jelgava)
                LocalTime outboundStart = currentTime;
                LocalTime outboundEnd = outboundStart.plusMinutes(tripDurationMinutes);
                
                int pasazieriOut = calculateTripPassengers(
                        stacijuIds, marsruts.getId(), outboundStart, cilvekuPieprasijumi);
                Map<Long, LocalTime> pieturuLaikiOut = calculateStopTimes(stacijuIds, outboundStart, stacijuMapa);
                
                braucieni.add(new Brauciens(
                        id++,
                        marsruts.getId(),
                        outboundStart,
                        outboundEnd,
                        firstStationId,
                        lastStationId,
                        new ArrayList<>(stacijuIds),
                        pasazieriOut,
                        pieturuLaikiOut
                ));
                
                // RETURN trip (Jelgava → Rīga) - starts after turnaround
                LocalTime returnStart = outboundEnd.plusMinutes(turnaroundMinutes);
                LocalTime returnEnd = returnStart.plusMinutes(tripDurationMinutes);
                
                // Only add return trip if it doesn't go past end hour
                if (returnEnd.getHour() <= endHour + 1) {
                    List<Long> reversedStations = new ArrayList<>(stacijuIds);
                    java.util.Collections.reverse(reversedStations);
                    
                    int pasazieriRet = calculateTripPassengers(
                            reversedStations, marsruts.getId(), returnStart, cilvekuPieprasijumi);
                    Map<Long, LocalTime> pieturuLaikiRet = calculateStopTimes(reversedStations, returnStart, stacijuMapa);
                    
                    braucieni.add(new Brauciens(
                            id++,
                            marsruts.getId(),
                            returnStart,
                            returnEnd,
                            lastStationId,
                            firstStationId,
                            new ArrayList<>(reversedStations),
                            pasazieriRet,
                            pieturuLaikiRet
                    ));
                }
                
                // Move to next cycle start time
                currentTime = currentTime.plusMinutes(intervalHours * 60);
            }
        }
        
        return braucieni;
    }
    
    /**
     * Calculate total trip duration in minutes.
     */
    private static int calculateTripDuration(List<Long> stacijuIds, Map<Long, Stacija> stacijuMapa) {
        int totalMinutes = 0;
        
        for (int i = 0; i < stacijuIds.size() - 1; i++) {
            Stacija from = stacijuMapa.get(stacijuIds.get(i));
            Stacija to = stacijuMapa.get(stacijuIds.get(i + 1));
            
            if (from != null && to != null) {
                double distanceKm = from.getKoordinatas().distanceKm(to.getKoordinatas());
                int travelMinutes = (int) Math.round((distanceKm / DEFAULT_SPEED_KMH) * 60);
                totalMinutes += travelMinutes + STOP_TIME_MINUTES;
            }
        }
        
        return Math.max(totalMinutes, 30); // Minimum 30 min trip
    }
    
    /**
     * Calculate arrival time at each station along the route.
     * Returns a map: stationId -> arrivalTime
     */
    private static Map<Long, LocalTime> calculateStopTimes(
            List<Long> stacijuIds, 
            LocalTime startTime,
            Map<Long, Stacija> stacijuMapa) {
        
        // Use LinkedHashMap to preserve order
        Map<Long, LocalTime> stopTimes = new LinkedHashMap<>();
        LocalTime currentTime = startTime;
        
        for (int i = 0; i < stacijuIds.size(); i++) {
            Long stationId = stacijuIds.get(i);
            stopTimes.put(stationId, currentTime);
            
            // Calculate travel time to next station
            if (i < stacijuIds.size() - 1) {
                Stacija from = stacijuMapa.get(stationId);
                Stacija to = stacijuMapa.get(stacijuIds.get(i + 1));
                
                if (from != null && to != null) {
                    double distanceKm = from.getKoordinatas().distanceKm(to.getKoordinatas());
                    int travelMinutes = (int) Math.round((distanceKm / DEFAULT_SPEED_KMH) * 60);
                    currentTime = currentTime.plusMinutes(travelMinutes + STOP_TIME_MINUTES);
                }
            }
        }
        
        return stopTimes;
    }
    
    /**
     * Calculate expected passengers for a trip.
     */
    private static int calculateTripPassengers(
            List<Long> stacijuIds,
            Long marsrutaId,
            LocalTime tripTime,
            List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        
        return cilvekuPieprasijumi.stream()
                .filter(p -> p.getMarsrutaId().equals(marsrutaId))
                .filter(p -> stacijuIds.contains(p.getStacijasId()))
                .filter(p -> p.getStunda().getHour() == tripTime.getHour())
                .mapToInt(CilvekuPieprasijums::getCilvekuSkaits)
                .sum();
    }
}
