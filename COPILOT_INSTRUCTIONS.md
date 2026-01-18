# COPILOT INSTRUCTIONS - Rolling Stock Rostering Project

## QUICK REFERENCE
This project transforms `flight-crew-scheduling` into a Latvian train scheduling system.

**Package:** `org.acme.rollingstockrostering`

## TASK 1: Delete Old Files
Delete everything in `src/main/java/org/acme/flightcrewscheduling/`

## TASK 2: Create Domain Classes

Create these files in `src/main/java/org/acme/rollingstockrostering/domain/`:

| File | Type | Key Fields |
|------|------|------------|
| GeoCoordinates.java | Helper | latitude, longitude, distanceKm() method |
| Stacija.java | Problem Fact | id, nosaukums, koordinatas, kaiminiStacijas |
| Vilciens.java | Problem Fact | id, kapacitate |
| Marsruts.java | Problem Fact | id, nosaukums, stacijas (List<Long>) |
| Depo.java | Problem Fact | id, vilciensId, stacijaId |
| Konfiguracija.java | Problem Fact | minimālaisIntervāls, stāvēšanasLaiks |
| CilvekuPieprasijums.java | Problem Fact | id, stacijasId, marsrutaId, stunda, cilvekuSkaits |
| **AtiesanasLaiks.java** | **@PlanningEntity** | id, stacijasId, marsrutaId, laiks, cilvekuDelta, **vilcienaId (@PlanningVariable)** |
| **RollingStockSchedule.java** | **@PlanningSolution** | vilcieni, stacijas, marsruti, depiList, cilvekuPieprasijumi, konfiguracija, atiesanasLaiki, score |

## TASK 3: Key Annotations

**AtiesanasLaiks.java:**
```java
@PlanningEntity
public class AtiesanasLaiks {
    @PlanningId
    private Long id;
    
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange", nullable = true)
    private Long vilcienaId;
    // ... other fields
}
```

**RollingStockSchedule.java:**
```java
@PlanningSolution
public class RollingStockSchedule {
    @ValueRangeProvider(id = "vilcienuRange")
    @ProblemFactCollectionProperty
    private List<Vilciens> vilcieni;
    
    @PlanningEntityCollectionProperty
    private List<AtiesanasLaiks> atiesanasLaiki;
    
    @PlanningScore
    private HardSoftScore score;
    // ... other fields with @ProblemFactCollectionProperty
}
```

## TASK 4: Create Constraint Provider

File: `src/main/java/org/acme/rollingstockrostering/solver/VilcienuConstraintProvider.java`

**Constraints to implement:**
- HARD: `minimālaisIntervāls` - Min 5 min between trains at same station
- SOFT: `maksimizētPasažierus` - Reward for passengers served
- SOFT: `minimizētVilcienus` - Penalize number of trains used

## TASK 5: Create REST Resource

File: `src/main/java/org/acme/rollingstockrostering/rest/ScheduleResource.java`

**Endpoints:**
- GET /schedule - Return current schedule
- POST /schedule/solve - Start solver
- POST /schedule/stop - Stop solver
- GET /schedule/status - Return solver status

## TASK 6: Add DemoDataGenerator.java

Copy provided `DemoDataGenerator.java` to `rest/` folder.
Contains 148 Latvian stations, 10 routes, coordinate-based travel times.

## TASK 7: Update application.properties

```properties
quarkus.timefold.solver.termination.spent-limit=5m
quarkus.http.cors=true
quarkus.http.cors.origins=*
```

## RUN COMMANDS

```bash
# Development
./mvnw quarkus:dev

# Test
./mvnw test

# Build
./mvnw package
```

## IMPORTS REFERENCE

```java
// Timefold imports
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
```
