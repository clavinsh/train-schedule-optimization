package org.acme.RollingStockRosteringOptimization.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private static final int[] TRAIN_CAPACITIES = {100, 150, 200, 250, 300};
    private static final int RIDES_PER_ROUTE_MIN = 8;
    private static final int RIDES_PER_ROUTE_MAX = 12;
    private static final int TRAIN_COUNT = 20;

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

    private Map<String, Station> createStations() {
        Map<String, Station> stations = new HashMap<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        // Central hub
        stations.put("Rīga", new Station(String.valueOf(idCounter.getAndIncrement()), "Rīga"));

        // Route: Rīga - Indra | Zilupe | Gulbene (shared segment)
        String[] sharedEastern = {"Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils"};
        for (String name : sharedEastern) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Indra (from Krustpils)
        String[] indraRoute = {"Trepe", "Līvāni", "Jersika", "Nīcgale", "Vabole", "Līksna",
                "Daugavpils", "Krāslava", "Indra"};
        for (String name : indraRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Zilupe (from Krustpils)
        String[] zilupeRoute = {"Kūkas", "Mežāre", "Atašiene", "Stirniene", "Varakļāni", "Viļāni",
                "Sakstagals", "Rēzekne", "Taudejāni", "Cirma", "Ludza", "Istalsna", "Nerza", "Briģi", "Zilupe"};
        for (String name : zilupeRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Gulbene (from Krustpils)
        String[] gulbeneRoute = {"Jaunkalsnava", "Kalsnava", "Mārciena", "Madona", "Cesvaine",
                "Jaungulbene", "Gulbene"};
        for (String name : gulbeneRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Liepāja | Tukums II (shared start)
        stations.put("Torņakalns", new Station(String.valueOf(idCounter.getAndIncrement()), "Torņakalns"));

        // Route: Rīga - Liepāja
        String[] liepajaRoute = {"Bieriņi / Bērnu slimnīca", "BA Turība", "Tīraine", "Medemciems",
                "Jaunolaine", "Olaine", "Dalbe", "Cena", "Ozolnieki", "Cukurfabrika", "Jelgava",
                "Dobele", "Biksti", "Saldus", "Skrunda", "Liepāja"};
        for (String name : liepajaRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Tukums II
        String[] tukumsRoute = {"Zasulauks", "Depo", "Zolitūde", "Imanta", "Babīte", "Priedaine",
                "Lielupe", "Bulduri", "Dzintari", "Majori", "Dubulti", "Jaundubulti", "Pumpuri",
                "Melluži", "Asari", "Vaivari", "Sloka", "Kūdra", "Ķemeri", "Smārde", "Milzkalne",
                "Tukums I", "Tukums II"};
        for (String name : tukumsRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Valga | Skulte (shared start)
        stations.put("Zemitāni", new Station(String.valueOf(idCounter.getAndIncrement()), "Zemitāni"));

        // Route: Rīga - Valga
        String[] valgaRoute = {"Čiekurkalns", "Šmerlis", "Jugla", "Garkalne", "Krievupe", "Vangaži",
                "Inčukalns", "Egļupe", "Sigulda", "Līgatne", "Ieriķi", "Melturi", "Āraiši", "Cēsis",
                "Jānamuiža", "Lode", "Valmiera", "Strenči", "Lugaži", "Valga"};
        for (String name : valgaRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        // Route: Rīga - Skulte
        String[] skulteRoute = {"Brasa", "Sarkandaugava", "Dauderi", "Mangaļi", "Ziemeļblāzma",
                "Vecdaugava", "Vecāķi", "Kalngale", "Garciems", "Garupe", "Carnikava", "Gauja",
                "Lilaste", "Inčupe", "Pabaži", "Saulkrasti", "Ķīšupe", "Zvejniekciems", "Skulte"};
        for (String name : skulteRoute) {
            stations.put(name, new Station(String.valueOf(idCounter.getAndIncrement()), name));
        }

        return stations;
    }

    private void setupNeighborDistances(Map<String, Station> stationMap) {
        // Helper to add bidirectional connection
        java.util.function.BiConsumer<String, String> connect = (from, to) -> {
            Station fromStation = stationMap.get(from);
            Station toStation = stationMap.get(to);
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
        };

        // Rīga connections
        connect.accept("Rīga", "Vagonu parks");
        connect.accept("Rīga", "Torņakalns");
        connect.accept("Rīga", "Zemitāni");

        // Eastern route (Rīga -> Krustpils)
        String[] easternStations = {"Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils"};
        for (int i = 0; i < easternStations.length - 1; i++) {
            connect.accept(easternStations[i], easternStations[i + 1]);
        }

        // Krustpils connections to branch routes
        connect.accept("Krustpils", "Trepe");
        connect.accept("Krustpils", "Kūkas");
        connect.accept("Krustpils", "Jaunkalsnava");

        // Indra route (from Krustpils)
        String[] indraStations = {"Trepe", "Līvāni", "Jersika", "Nīcgale", "Vabole", "Līksna",
                "Daugavpils", "Krāslava", "Indra"};
        for (int i = 0; i < indraStations.length - 1; i++) {
            connect.accept(indraStations[i], indraStations[i + 1]);
        }

        // Zilupe route (from Krustpils)
        String[] zilupeStations = {"Kūkas", "Mežāre", "Atašiene", "Stirniene", "Varakļāni", "Viļāni",
                "Sakstagals", "Rēzekne", "Taudejāni", "Cirma", "Ludza", "Istalsna", "Nerza", "Briģi", "Zilupe"};
        for (int i = 0; i < zilupeStations.length - 1; i++) {
            connect.accept(zilupeStations[i], zilupeStations[i + 1]);
        }

        // Gulbene route (from Krustpils)
        String[] gulbeneStations = {"Jaunkalsnava", "Kalsnava", "Mārciena", "Madona", "Cesvaine",
                "Jaungulbene", "Gulbene"};
        for (int i = 0; i < gulbeneStations.length - 1; i++) {
            connect.accept(gulbeneStations[i], gulbeneStations[i + 1]);
        }

        // Torņakalns connections
        connect.accept("Torņakalns", "Bieriņi / Bērnu slimnīca");
        connect.accept("Torņakalns", "Zasulauks");

        // Liepāja route
        String[] liepajaStations = {"Bieriņi / Bērnu slimnīca", "BA Turība", "Tīraine", "Medemciems",
                "Jaunolaine", "Olaine", "Dalbe", "Cena", "Ozolnieki", "Cukurfabrika", "Jelgava",
                "Dobele", "Biksti", "Saldus", "Skrunda", "Liepāja"};
        for (int i = 0; i < liepajaStations.length - 1; i++) {
            connect.accept(liepajaStations[i], liepajaStations[i + 1]);
        }

        // Tukums route
        String[] tukumsStations = {"Zasulauks", "Depo", "Zolitūde", "Imanta", "Babīte", "Priedaine",
                "Lielupe", "Bulduri", "Dzintari", "Majori", "Dubulti", "Jaundubulti", "Pumpuri",
                "Melluži", "Asari", "Vaivari", "Sloka", "Kūdra", "Ķemeri", "Smārde", "Milzkalne",
                "Tukums I", "Tukums II"};
        for (int i = 0; i < tukumsStations.length - 1; i++) {
            connect.accept(tukumsStations[i], tukumsStations[i + 1]);
        }

        // Zemitāni connections
        connect.accept("Zemitāni", "Čiekurkalns");
        connect.accept("Zemitāni", "Brasa");

        // Valga route
        String[] valgaStations = {"Čiekurkalns", "Šmerlis", "Jugla", "Garkalne", "Krievupe", "Vangaži",
                "Inčukalns", "Egļupe", "Sigulda", "Līgatne", "Ieriķi", "Melturi", "Āraiši", "Cēsis",
                "Jānamuiža", "Lode", "Valmiera", "Strenči", "Lugaži", "Valga"};
        for (int i = 0; i < valgaStations.length - 1; i++) {
            connect.accept(valgaStations[i], valgaStations[i + 1]);
        }

        // Skulte route
        String[] skulteStations = {"Brasa", "Sarkandaugava", "Dauderi", "Mangaļi", "Ziemeļblāzma",
                "Vecdaugava", "Vecāķi", "Kalngale", "Garciems", "Garupe", "Carnikava", "Gauja",
                "Lilaste", "Inčupe", "Pabaži", "Saulkrasti", "Ķīšupe", "Zvejniekciems", "Skulte"};
        for (int i = 0; i < skulteStations.length - 1; i++) {
            connect.accept(skulteStations[i], skulteStations[i + 1]);
        }
    }

    private List<Route> createRoutes(Map<String, Station> stationMap) {
        List<Route> routes = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        // Route 1: Rīga - Indra
        routes.add(createRoute(idCounter, "Rīga - Indra", stationMap,
                "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                "Trepe", "Līvāni", "Jersika", "Nīcgale", "Vabole", "Līksna", "Daugavpils", "Krāslava", "Indra"));

        // Route 2: Rīga - Zilupe
        routes.add(createRoute(idCounter, "Rīga - Zilupe", stationMap,
                "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                "Kūkas", "Mežāre", "Atašiene", "Stirniene", "Varakļāni", "Viļāni", "Sakstagals",
                "Rēzekne", "Taudejāni", "Cirma", "Ludza", "Istalsna", "Nerza", "Briģi", "Zilupe"));

        // Route 3: Rīga - Gulbene
        routes.add(createRoute(idCounter, "Rīga - Gulbene", stationMap,
                "Rīga", "Vagonu parks", "Jāņavārti", "Daugmale", "Šķirotava", "Gaisma",
                "Rumbula", "Dārziņi", "Dole", "Salaspils", "Saulkalne", "Ikšķile", "Jaunogre",
                "Ogre", "Pārogre", "Ciemupe", "Ķegums", "Lielvārde", "Kaibala", "Jumprava",
                "Skrīveri", "Muldakmens", "Aizkraukle", "Koknese", "Alotene", "Pļaviņas", "Krustpils",
                "Jaunkalsnava", "Kalsnava", "Mārciena", "Madona", "Cesvaine", "Jaungulbene", "Gulbene"));

        // Route 4: Rīga - Liepāja
        routes.add(createRoute(idCounter, "Rīga - Liepāja", stationMap,
                "Rīga", "Torņakalns", "Bieriņi / Bērnu slimnīca", "BA Turība", "Tīraine",
                "Medemciems", "Jaunolaine", "Olaine", "Dalbe", "Cena", "Ozolnieki", "Cukurfabrika",
                "Jelgava", "Dobele", "Biksti", "Saldus", "Skrunda", "Liepāja"));

        // Route 5: Rīga - Tukums II
        routes.add(createRoute(idCounter, "Rīga - Tukums II", stationMap,
                "Rīga", "Torņakalns", "Zasulauks", "Depo", "Zolitūde", "Imanta", "Babīte",
                "Priedaine", "Lielupe", "Bulduri", "Dzintari", "Majori", "Dubulti", "Jaundubulti",
                "Pumpuri", "Melluži", "Asari", "Vaivari", "Sloka", "Kūdra", "Ķemeri", "Smārde",
                "Milzkalne", "Tukums I", "Tukums II"));

        // Route 6: Rīga - Valga
        routes.add(createRoute(idCounter, "Rīga - Valga", stationMap,
                "Rīga", "Zemitāni", "Čiekurkalns", "Šmerlis", "Jugla", "Garkalne", "Krievupe",
                "Vangaži", "Inčukalns", "Egļupe", "Sigulda", "Līgatne", "Ieriķi", "Melturi",
                "Āraiši", "Cēsis", "Jānamuiža", "Lode", "Valmiera", "Strenči", "Lugaži", "Valga"));

        // Route 7: Rīga - Skulte
        routes.add(createRoute(idCounter, "Rīga - Skulte", stationMap,
                "Rīga", "Zemitāni", "Brasa", "Sarkandaugava", "Dauderi", "Mangaļi", "Ziemeļblāzma",
                "Vecdaugava", "Vecāķi", "Kalngale", "Garciems", "Garupe", "Carnikava", "Gauja",
                "Lilaste", "Inčupe", "Pabaži", "Saulkrasti", "Ķīšupe", "Zvejniekciems", "Skulte"));

        return routes;
    }

    private Route createRoute(AtomicInteger idCounter, String name, Map<String, Station> stationMap,
                              String... stationNames) {
        List<Station> stations = new ArrayList<>();
        for (String stationName : stationNames) {
            Station station = stationMap.get(stationName);
            if (station != null) {
                stations.add(station);
            }
        }
        return new Route(String.valueOf(idCounter.getAndIncrement()), name, stations);
    }

    private List<Depo> createDepos(Map<String, Station> stationMap) {
        List<Depo> depos = new ArrayList<>();

        // Main depot at Rīga
        depos.add(new Depo("1", stationMap.get("Rīga")));
        // Eastern hub depot at Krustpils
        depos.add(new Depo("2", stationMap.get("Krustpils")));
        // Western depot at Jelgava
        depos.add(new Depo("3", stationMap.get("Jelgava")));

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

                // Random passenger count (50-200)
                int passengerCount = 50 + random.nextInt(151);

                Ride ride = new Ride(
                        String.valueOf(rideCounter.getAndIncrement()),
                        route,
                        departureStation,
                        arrivalStation,
                        departureTime,
                        arrivalTime
                );
                ride.setPassengerCount(passengerCount);
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

        // Major stations with higher demand
        String[] majorStations = {"Rīga", "Krustpils", "Jelgava", "Daugavpils", "Rēzekne",
                "Liepāja", "Sigulda", "Cēsis", "Valmiera", "Ogre", "Majori", "Gulbene"};

        for (String stationName : majorStations) {
            Station station = stationMap.get(stationName);
            if (station != null) {
                Map<Integer, Integer> hourlyDemand = new HashMap<>();

                // Generate demand for each hour (5:00 to 23:00)
                for (int hour = 5; hour <= 23; hour++) {
                    int baseDemand;
                    if (stationName.equals("Rīga")) {
                        baseDemand = 200 + random.nextInt(300); // High demand for capital
                    } else if (hour >= 7 && hour <= 9 || hour >= 16 && hour <= 18) {
                        baseDemand = 100 + random.nextInt(150); // Rush hours
                    } else {
                        baseDemand = 30 + random.nextInt(70); // Off-peak
                    }
                    hourlyDemand.put(hour, baseDemand);
                }

                demands.add(new Demand(String.valueOf(idCounter.getAndIncrement()), station, hourlyDemand));
            }
        }

        return demands;
    }
}
