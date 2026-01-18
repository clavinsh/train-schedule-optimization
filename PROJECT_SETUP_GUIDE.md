# Rolling Stock Rostering - Project Setup Guide

## Overview
This guide transforms the Timefold `flight-crew-scheduling` quickstart into a **Latvian Train Scheduling Optimization** project.

---

## Step 1: Clone the Base Project

```bash
# Clone only the flight-crew-scheduling folder
git clone --depth 1 --filter=blob:none --sparse https://github.com/TimefoldAI/timefold-quickstarts.git
cd timefold-quickstarts
git sparse-checkout set java/flight-crew-scheduling

# Copy to new project folder
cp -r java/flight-crew-scheduling ../rolling-stock-rostering
cd ../rolling-stock-rostering

# Remove git history (start fresh)
rm -rf .git
git init
```

---

## Step 2: Project Structure Changes

### Files to DELETE:
```
src/main/java/org/acme/flightcrewscheduling/
├── domain/
│   ├── Aircraft.java              # DELETE
│   ├── Airport.java               # DELETE - replace with Stacija
│   ├── Employee.java              # DELETE
│   ├── Flight.java                # DELETE - replace with AtiesanasLaiks
│   ├── FlightAssignment.java      # DELETE
│   └── FlightCrewSchedule.java    # DELETE - replace with RollingStockSchedule
├── solver/
│   └── FlightCrewSchedulingConstraintProvider.java  # DELETE - replace
└── rest/
    └── FlightCrewSchedulingResource.java            # DELETE - replace
```

### Files to CREATE:
```
src/main/java/org/acme/rollingstockrostering/
├── domain/
│   ├── Stacija.java               # Station with coordinates
│   ├── GeoCoordinates.java        # Lat/Lon helper class
│   ├── Vilciens.java              # Train with capacity
│   ├── Marsruts.java              # Route (list of stations)
│   ├── Depo.java                  # Depot location
│   ├── Konfiguracija.java         # Configuration parameters
│   ├── CilvekuPieprasijums.java   # Passenger demand
│   ├── AtiesanasLaiks.java        # @PlanningEntity - departure time
│   └── RollingStockSchedule.java  # @PlanningSolution
├── solver/
│   └── VilcienuConstraintProvider.java  # Constraints
└── rest/
    ├── DemoDataGenerator.java     # Test data (provided)
    └── ScheduleResource.java      # REST API
```

---

## Step 3: Rename Package

### In pom.xml:
Change groupId and artifactId:
```xml
<groupId>org.acme</groupId>
<artifactId>rolling-stock-rostering</artifactId>
<version>1.0-SNAPSHOT</version>
<name>Rolling Stock Rostering</name>
```

### Rename folder structure:
```bash
# Rename package folders
mv src/main/java/org/acme/flightcrewscheduling src/main/java/org/acme/rollingstockrostering
```

### In application.properties:
```properties
quarkus.timefold.solver.termination.spent-limit=5m

# Enable CORS for frontend
quarkus.http.cors=true
quarkus.http.cors.origins=*
```

---

## Step 4: Domain Classes Implementation

### 4.1 GeoCoordinates.java
```java
package org.acme.rollingstockrostering.domain;

public class GeoCoordinates {
    private double latitude;
    private double longitude;

    public GeoCoordinates() {}

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Calculate distance to another coordinate using Haversine formula
     * @return distance in kilometers
     */
    public double distanceKm(GeoCoordinates other) {
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(this.latitude)) * 
                   Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Getters and setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
```

### 4.2 Stacija.java
```java
package org.acme.rollingstockrostering.domain;

import java.util.List;

public class Stacija {
    private Long id;
    private String nosaukums;
    private GeoCoordinates koordinatas;
    private List<Long> kaiminiStacijas;

    public Stacija() {}

    public Stacija(Long id, String nosaukums, GeoCoordinates koordinatas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.koordinatas = koordinatas;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNosaukums() { return nosaukums; }
    public void setNosaukums(String nosaukums) { this.nosaukums = nosaukums; }
    public GeoCoordinates getKoordinatas() { return koordinatas; }
    public void setKoordinatas(GeoCoordinates koordinatas) { this.koordinatas = koordinatas; }
    public List<Long> getKaiminiStacijas() { return kaiminiStacijas; }
    public void setKaiminiStacijas(List<Long> kaiminiStacijas) { this.kaiminiStacijas = kaiminiStacijas; }
}
```

### 4.3 Vilciens.java
```java
package org.acme.rollingstockrostering.domain;

public class Vilciens {
    private Long id;
    private int kapacitate;

    public Vilciens() {}

    public Vilciens(Long id, int kapacitate) {
        this.id = id;
        this.kapacitate = kapacitate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getKapacitate() { return kapacitate; }
    public void setKapacitate(int kapacitate) { this.kapacitate = kapacitate; }
}
```

### 4.4 Marsruts.java
```java
package org.acme.rollingstockrostering.domain;

import java.util.List;

public class Marsruts {
    private Long id;
    private String nosaukums;
    private List<Long> stacijas;  // Ordered list of station IDs

    public Marsruts() {}

    public Marsruts(Long id, String nosaukums, List<Long> stacijas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.stacijas = stacijas;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNosaukums() { return nosaukums; }
    public void setNosaukums(String nosaukums) { this.nosaukums = nosaukums; }
    public List<Long> getStacijas() { return stacijas; }
    public void setStacijas(List<Long> stacijas) { this.stacijas = stacijas; }
}
```

### 4.5 Depo.java
```java
package org.acme.rollingstockrostering.domain;

public class Depo {
    private Long id;
    private Long vilciensId;
    private Long stacijaId;

    public Depo() {}

    public Depo(Long id, Long vilciensId, Long stacijaId) {
        this.id = id;
        this.vilciensId = vilciensId;
        this.stacijaId = stacijaId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVilciensId() { return vilciensId; }
    public void setVilciensId(Long vilciensId) { this.vilciensId = vilciensId; }
    public Long getStacijaId() { return stacijaId; }
    public void setStacijaId(Long stacijaId) { this.stacijaId = stacijaId; }
}
```

### 4.6 Konfiguracija.java
```java
package org.acme.rollingstockrostering.domain;

import java.time.Duration;

public class Konfiguracija {
    private Duration minimālaisIntervāls;  // Min time between trains at same station
    private Duration stāvēšanasLaiks;      // Stop duration at each station

    public Konfiguracija() {}

    public Konfiguracija(Duration minimālaisIntervāls, Duration stāvēšanasLaiks) {
        this.minimālaisIntervāls = minimālaisIntervāls;
        this.stāvēšanasLaiks = stāvēšanasLaiks;
    }

    // Getters and setters
    public Duration getMinimālaisIntervāls() { return minimālaisIntervāls; }
    public void setMinimālaisIntervāls(Duration minimālaisIntervāls) { this.minimālaisIntervāls = minimālaisIntervāls; }
    public Duration getStāvēšanasLaiks() { return stāvēšanasLaiks; }
    public void setStāvēšanasLaiks(Duration stāvēšanasLaiks) { this.stāvēšanasLaiks = stāvēšanasLaiks; }
}
```

### 4.7 CilvekuPieprasijums.java
```java
package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;

public class CilvekuPieprasijums {
    private Long id;
    private Long stacijasId;
    private Long marsrutaId;
    private LocalTime stunda;
    private int cilvekuSkaits;

    public CilvekuPieprasijums() {}

    public CilvekuPieprasijums(Long id, Long stacijasId, Long marsrutaId, 
                               LocalTime stunda, int cilvekuSkaits) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.stunda = stunda;
        this.cilvekuSkaits = cilvekuSkaits;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStacijasId() { return stacijasId; }
    public void setStacijasId(Long stacijasId) { this.stacijasId = stacijasId; }
    public Long getMarsrutaId() { return marsrutaId; }
    public void setMarsrutaId(Long marsrutaId) { this.marsrutaId = marsrutaId; }
    public LocalTime getStunda() { return stunda; }
    public void setStunda(LocalTime stunda) { this.stunda = stunda; }
    public int getCilvekuSkaits() { return cilvekuSkaits; }
    public void setCilvekuSkaits(int cilvekuSkaits) { this.cilvekuSkaits = cilvekuSkaits; }
}
```

### 4.8 AtiesanasLaiks.java (PLANNING ENTITY)
```java
package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class AtiesanasLaiks {
    
    @PlanningId
    private Long id;
    
    private Long stacijasId;
    private Long marsrutaId;
    private LocalTime laiks;
    private int cilvekuDelta;  // Expected passenger change at this stop
    
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange", nullable = true)
    private Long vilcienaId;  // This is what Timefold assigns

    public AtiesanasLaiks() {}

    public AtiesanasLaiks(Long id, Long stacijasId, Long marsrutaId, 
                          LocalTime laiks, int cilvekuDelta) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.laiks = laiks;
        this.cilvekuDelta = cilvekuDelta;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStacijasId() { return stacijasId; }
    public void setStacijasId(Long stacijasId) { this.stacijasId = stacijasId; }
    public Long getMarsrutaId() { return marsrutaId; }
    public void setMarsrutaId(Long marsrutaId) { this.marsrutaId = marsrutaId; }
    public LocalTime getLaiks() { return laiks; }
    public void setLaiks(LocalTime laiks) { this.laiks = laiks; }
    public int getCilvekuDelta() { return cilvekuDelta; }
    public void setCilvekuDelta(int cilvekuDelta) { this.cilvekuDelta = cilvekuDelta; }
    public Long getVilcienaId() { return vilcienaId; }
    public void setVilcienaId(Long vilcienaId) { this.vilcienaId = vilcienaId; }
}
```

### 4.9 RollingStockSchedule.java (PLANNING SOLUTION)
```java
package org.acme.rollingstockrostering.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class RollingStockSchedule {
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "vilcienuRange")
    private List<Vilciens> vilcieni;
    
    @ProblemFactCollectionProperty
    private List<Stacija> stacijas;
    
    @ProblemFactCollectionProperty
    private List<Marsruts> marsruti;
    
    @ProblemFactCollectionProperty
    private List<Depo> depiList;
    
    @ProblemFactCollectionProperty
    private List<CilvekuPieprasijums> cilvekuPieprasijumi;
    
    @ProblemFactProperty
    private Konfiguracija konfiguracija;
    
    @PlanningEntityCollectionProperty
    private List<AtiesanasLaiks> atiesanasLaiki;
    
    @PlanningScore
    private HardSoftScore score;

    public RollingStockSchedule() {}

    public RollingStockSchedule(List<Vilciens> vilcieni, List<Stacija> stacijas,
                                List<Marsruts> marsruti, List<Depo> depiList,
                                List<CilvekuPieprasijums> cilvekuPieprasijumi,
                                Konfiguracija konfiguracija,
                                List<AtiesanasLaiks> atiesanasLaiki) {
        this.vilcieni = vilcieni;
        this.stacijas = stacijas;
        this.marsruti = marsruti;
        this.depiList = depiList;
        this.cilvekuPieprasijumi = cilvekuPieprasijumi;
        this.konfiguracija = konfiguracija;
        this.atiesanasLaiki = atiesanasLaiki;
    }

    // Getters and setters
    public List<Vilciens> getVilcieni() { return vilcieni; }
    public void setVilcieni(List<Vilciens> vilcieni) { this.vilcieni = vilcieni; }
    public List<Stacija> getStacijas() { return stacijas; }
    public void setStacijas(List<Stacija> stacijas) { this.stacijas = stacijas; }
    public List<Marsruts> getMarsruti() { return marsruti; }
    public void setMarsruti(List<Marsruts> marsruti) { this.marsruti = marsruti; }
    public List<Depo> getDepiList() { return depiList; }
    public void setDepiList(List<Depo> depiList) { this.depiList = depiList; }
    public List<CilvekuPieprasijums> getCilvekuPieprasijumi() { return cilvekuPieprasijumi; }
    public void setCilvekuPieprasijumi(List<CilvekuPieprasijums> cilvekuPieprasijumi) { this.cilvekuPieprasijumi = cilvekuPieprasijumi; }
    public Konfiguracija getKonfiguracija() { return konfiguracija; }
    public void setKonfiguracija(Konfiguracija konfiguracija) { this.konfiguracija = konfiguracija; }
    public List<AtiesanasLaiks> getAtiesanasLaiki() { return atiesanasLaiki; }
    public void setAtiesanasLaiki(List<AtiesanasLaiks> atiesanasLaiki) { this.atiesanasLaiki = atiesanasLaiki; }
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }
}
```

---

## Step 5: Constraint Provider

### VilcienuConstraintProvider.java
```java
package org.acme.rollingstockrostering.solver;

import java.time.Duration;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.rollingstockrostering.domain.AtiesanasLaiks;

public class VilcienuConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            // Hard constraints
            minimālaisIntervāls(factory),
            
            // Soft constraints
            maksimizētPasažierus(factory),
            minimizētVilcienus(factory)
        };
    }

    /**
     * HARD: Minimum 5 minutes between trains at same station on same route
     */
    Constraint minimālaisIntervāls(ConstraintFactory factory) {
        return factory.forEachUniquePair(AtiesanasLaiks.class,
                Joiners.equal(AtiesanasLaiks::getStacijasId),
                Joiners.equal(AtiesanasLaiks::getMarsrutaId),
                Joiners.equal(AtiesanasLaiks::getVilcienaId))
            .filter((a1, a2) -> {
                if (a1.getVilcienaId() == null || a2.getVilcienaId() == null) {
                    return false;
                }
                Duration diff = Duration.between(a1.getLaiks(), a2.getLaiks()).abs();
                return diff.toMinutes() < 5;
            })
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Minimālais intervāls starp vilcieniem");
    }

    /**
     * SOFT: Maximize passengers served (reward for each passenger)
     */
    Constraint maksimizētPasažierus(ConstraintFactory factory) {
        return factory.forEach(AtiesanasLaiks.class)
            .filter(a -> a.getVilcienaId() != null)
            .filter(a -> a.getCilvekuDelta() > 0)
            .reward(HardSoftScore.ONE_SOFT, AtiesanasLaiks::getCilvekuDelta)
            .asConstraint("Pasažieru maksimizācija");
    }

    /**
     * SOFT: Minimize number of trains used (penalize each unique train)
     */
    Constraint minimizētVilcienus(ConstraintFactory factory) {
        return factory.forEach(AtiesanasLaiks.class)
            .filter(a -> a.getVilcienaId() != null)
            .groupBy(AtiesanasLaiks::getVilcienaId)
            .penalize(HardSoftScore.ofSoft(100))
            .asConstraint("Vilcienu skaita minimizācija");
    }
}
```

---

## Step 6: REST Resource

### ScheduleResource.java
```java
package org.acme.rollingstockrostering.rest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.rollingstockrostering.domain.RollingStockSchedule;

@Path("/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @Inject
    SolverManager<RollingStockSchedule, String> solverManager;

    @Inject
    DemoDataGenerator demoDataGenerator;

    private ConcurrentMap<String, RollingStockSchedule> scheduleMap = new ConcurrentHashMap<>();
    private String currentJobId = null;

    @GET
    public RollingStockSchedule getSchedule() {
        if (currentJobId == null) {
            return demoDataGenerator.generateDemoData();
        }
        return scheduleMap.getOrDefault(currentJobId, demoDataGenerator.generateDemoData());
    }

    @POST
    @Path("/solve")
    public void solve() {
        currentJobId = UUID.randomUUID().toString();
        RollingStockSchedule problem = demoDataGenerator.generateDemoData();
        scheduleMap.put(currentJobId, problem);
        
        solverManager.solveBuilder()
            .withProblemId(currentJobId)
            .withProblemFinder(id -> scheduleMap.get(id))
            .withBestSolutionConsumer(solution -> scheduleMap.put(currentJobId, solution))
            .run();
    }

    @POST
    @Path("/stop")
    public void stop() {
        if (currentJobId != null) {
            solverManager.terminateEarly(currentJobId);
        }
    }

    @GET
    @Path("/status")
    public SolverStatus getStatus() {
        if (currentJobId == null) {
            return SolverStatus.NOT_SOLVING;
        }
        return solverManager.getSolverStatus(currentJobId);
    }
}
```

---

## Step 7: Solver Configuration

### src/main/resources/application.properties
```properties
# Timefold Solver configuration
quarkus.timefold.solver.termination.spent-limit=5m

# Solver configuration file (optional, can be overridden)
# quarkus.timefold.solver-config-xml=solverConfig.xml

# CORS for frontend development
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with

# Dev mode settings
quarkus.live-reload.instrumentation=true
```

### src/main/resources/solverConfig.xml (Optional - for benchmarking)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<solver xmlns="https://timefold.ai/xsd/solver">
  <solutionClass>org.acme.rollingstockrostering.domain.RollingStockSchedule</solutionClass>
  <entityClass>org.acme.rollingstockrostering.domain.AtiesanasLaiks</entityClass>
  
  <scoreDirectorFactory>
    <constraintProviderClass>org.acme.rollingstockrostering.solver.VilcienuConstraintProvider</constraintProviderClass>
  </scoreDirectorFactory>
  
  <termination>
    <secondsSpentLimit>300</secondsSpentLimit>
  </termination>
  
  <!-- Construction Heuristic -->
  <constructionHeuristic>
    <constructionHeuristicType>FIRST_FIT_DECREASING</constructionHeuristicType>
  </constructionHeuristic>
  
  <!-- Local Search - Change for benchmarking -->
  <localSearch>
    <!-- Option 1: Late Acceptance (default) -->
    <acceptor>
      <lateAcceptanceSize>400</lateAcceptanceSize>
    </acceptor>
    
    <!-- Option 2: Tabu Search (uncomment to use)
    <acceptor>
      <moveTabuSize>7</moveTabuSize>
    </acceptor>
    -->
    
    <!-- Option 3: Simulated Annealing (uncomment to use)
    <acceptor>
      <simulatedAnnealingStartingTemperature>0hard/1000soft</simulatedAnnealingStartingTemperature>
    </acceptor>
    -->
    
    <forager>
      <acceptedCountLimit>4</acceptedCountLimit>
    </forager>
  </localSearch>
</solver>
```

---

## Step 8: Add DemoDataGenerator.java

Copy the provided `DemoDataGenerator.java` file to:
```
src/main/java/org/acme/rollingstockrostering/rest/DemoDataGenerator.java
```

This file contains:
- 148 real Latvian train stations with coordinates
- 10 routes (Rīga-Liepāja, Rīga-Tukums, Rīga-Valga, etc.)
- 30-40 trains with varying capacities
- 5 depot locations
- Realistic passenger demand with rush hour patterns
- Haversine-based travel time calculations

---

## Step 9: Update pom.xml Dependencies

Make sure these dependencies are present:
```xml
<dependencies>
    <!-- Timefold Solver -->
    <dependency>
        <groupId>ai.timefold.solver</groupId>
        <artifactId>timefold-solver-quarkus</artifactId>
    </dependency>
    <dependency>
        <groupId>ai.timefold.solver</groupId>
        <artifactId>timefold-solver-quarkus-jackson</artifactId>
    </dependency>
    
    <!-- Quarkus REST -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest-jackson</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit5</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>ai.timefold.solver</groupId>
        <artifactId>timefold-solver-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Step 10: Run the Project

### Development mode:
```bash
./mvnw quarkus:dev
```

### Access endpoints:
- GET http://localhost:8080/schedule - Get current schedule
- POST http://localhost:8080/schedule/solve - Start solving
- POST http://localhost:8080/schedule/stop - Stop solving
- GET http://localhost:8080/schedule/status - Get solver status

### Build for production:
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Build Docker image:
```bash
./mvnw package -Dquarkus.container-image.build=true
```

---

## Step 11: Verification Checklist

After setup, verify:

- [ ] Project compiles: `./mvnw compile`
- [ ] Tests pass: `./mvnw test`
- [ ] Dev mode starts: `./mvnw quarkus:dev`
- [ ] GET /schedule returns data
- [ ] POST /schedule/solve starts optimization
- [ ] Score improves over time

---

## Troubleshooting

### "No value range provided for planning variable"
- Check that `@ValueRangeProvider(id = "vilcienuRange")` is on the `vilcieni` list
- Check that `@PlanningVariable(valueRangeProviderRefs = "vilcienuRange")` references the same ID

### "No planning entities found"
- Check that `AtiesanasLaiks` has `@PlanningEntity` annotation
- Check that `RollingStockSchedule` has `@PlanningEntityCollectionProperty` on `atiesanasLaiki`

### Solver returns null score
- Check that ConstraintProvider is registered in application.properties or solverConfig.xml
- Check that constraints don't have syntax errors

---

## Project Summary

| Component | Description |
|-----------|-------------|
| **Problem** | Assign trains to departure times to maximize passengers served |
| **Planning Entity** | `AtiesanasLaiks` (departure time at station) |
| **Planning Variable** | `vilcienaId` (which train serves this departure) |
| **Hard Constraints** | Minimum interval between trains |
| **Soft Constraints** | Maximize passengers, minimize trains used |
| **Data** | 148 stations, 10 routes, 30-40 trains |
