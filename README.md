# 🚆 Rolling Stock Rostering Optimization

Timefold Solver projekts vilcienu sastāvu grafiku plānošanai.

## 📋 Saturs

- [Par projektu](#par-projektu)
- [Prasības](#prasības)
- [Ātrā palaišana](#ātrā-palaišana)
- [REST API](#rest-api)
- [Benchmarking](#benchmarking)
- [Projekta struktūra](#projekta-struktūra)

## Par projektu

**Rolling Stock Rostering** – NP-grūta kombinatorikas optimizācijas problēma, kur jāpiešķir vilcieni atiešanas laikiem:
- ✅ Apmierina pasažieru pieprasījumu
- ✅ Ievēro vilcienu kapacitāti
- ✅ Optimizē resursu izmantošanu
- ✅ Minimizē tukšos braucienus

**Risinājums:** Timefold Solver ar 6 dažādiem Local Search algoritmiem.

## Projekta struktūra

### Domēna modelis

#### Planning Entity: `AtiesanasLaiks` (DepartureTime)
Galvenais plānošanas elements - konkrēts atiešanas laiks no stacijas pa maršrutu:
```java
@PlanningEntity
public class AtiesanasLaiks {
    @PlanningId
    private Long id;
    
    private Long stacijasId;        // No kuras stacijas
    private Long marsrutaId;        // Uz kuru maršrutu
    private LocalTime laiks;        // Cikos
    private int cilvekuDelta;       // Cik pasažieri iekāpj
    
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange")
    private Vilciens vilciens;      // ŠEIT TIMEFOLD PIEŠĶIR VILCIENU!
}
```

#### Planning Solution: `RollingStockSchedule`
Pilns problēmas un risinājuma apraksts:
```java
@PlanningSolution
public class RollingStockSchedule {
    @ValueRangeProvider(id = "vilcienuRange")
    private List<Vilciens> vilcieni;                    // Pieejamie vilcieni
    
    private List<Stacija> stacijas;                      // Staciju saraksts
    private List<Marsruts> marsruti;                     // Maršruti
    private List<Depo> depo;                             // Depo
    private List<CilvekuPieprasijums> cilvekuPieprasijumi; // Pasažieru pieprasījums
    
    @PlanningEntityCollectionProperty
    private List<AtiesanasLaiks> atiesanasLaiki;        // Ko optimizējam
    
    @PlanningScore
    private HardSoftScore score;                         // Cik labs risinājums
}
```

### Problem Facts (Nemainīgie dati)

- **Vilciens**: ID, kapacitāte (pasažieru skaits)
- **Stacija**: ID, nosaukums, GPS koordinātas, kaimiņi
- **Maršruts**: ID, nosaukums, staciju secība
- **Depo**: Kur vilciens uzsāk/beidz dienu
- **CilvekuPieprasijums**: Cik pasažieri vēlas braukt konkrētā laikā
- **Konfiguracija**: Sistēmas parametri (stāvēšanas laiks, buferis)

## Constraints (Ierobežojumi)

### Hard Constraints (obligāti jāizpilda - hard score = 0)

**Pašlaik visi DISABLED demonstrācijas nolūkā:**
1. **vilciensApmekleVisasStacijas** - Pārbauda, vai vilciens neapkalpo divas stacijas vienlaikus
2. **vilciensNeparsniezKapacitati** - Pārbauda, vai kapacitāte nav pārsniegta
3. **vilciensNonakDepo** - Pārbauda, vai vilciens dienas beigās ir depō

*Piezīme: Hard constraints atspējoti, lai demonstrētu solver darbību bez pārāk stingru ierobežojumu. Reālā sistēmā tie būtu aktīvi.*

### Soft Constraints (optimizācijas mērķi - maksimizēt soft score)

1. **minimizetTuksusBraucienus** - Penalizē braucienus bez pasažieriem (-10 punkti par katru tukšu braucienu)
2. **maksimizetPasazieruUznemsanu** - Atlīdzina par pasažieru pārvadāšanu (+1 punkts par katru pasažieri)

## Ātrā palaišana

### 1. Build projekts
```bash
./mvnw.cmd clean install
```

### 2. Palaid dev režīmā
```bash
./mvnw.cmd quarkus:dev
```

### 3. Atvēr pārlūkā
```
http://localhost:8080
```

### 4. Lieto UI
1. Spied "Sākt risināšanu"
2. Gaidi 30s (solver optimizē)
3. Apskati rezultātus trīs tabs:
   - **Pārskats**: Score, statistika
   - **Maršruti**: Vilcienu piešķīrumi pa maršrutiem
   - **Grafiks**: Reisu laika tabula
   - **JSON Dati**: Pilns risinājums

## REST API

| Metode | URL | Apraksts |
|--------|-----|----------|
| `GET` | `/rolling-stock-schedule` | Pašreizējais risinājums |
| `POST` | `/rolling-stock-schedule/solve` | Sākt solver |
| `GET` | `/rolling-stock-schedule/stop-solving` | Apturēt solver |
| `GET` | `/demo/rolling-stock-schedule` | Demo dati |

## Benchmarking

Salīdzina 6 algorit­mus uz 3 dataset izmēriem:

```bash
./mvnw.cmd exec:java -Dexec.mainClass="org.acme.rollingstockrostering.benchmark.RollingStockBenchmarkApp"
```

**Algoritmi:**
- First Fit Decreasing (baseline)
- Late Acceptance (400 & 800)
- Tabu Search  
- Simulated Annealing
- Hybrid (Tabu + Late Acceptance)

**Rezultāti:** `target/benchmark/index.html` (atveras automātiski)

## Projekta struktūra

```
src/main/java/org/acme/rollingstockrostering/
├── domain/                         # Domēna modelis
│   ├── AtiesanasLaiks.java        # @PlanningEntity
│   ├── RollingStockSchedule.java  # @PlanningSolution
│   ├── Vilciens.java              # Vilcieni
│   ├── Stacija.java               # Stacijas
│   └── Marsruts.java              # Maršruti
├── solver/
│   └── RollingStockConstraintProvider.java  # Constraints
└── rest/
    ├── RollingStockSchedulingResource.java  # REST API
    └── DemoDataGenerator.java               # Test data

src/main/resources/
├── benchmarkConfig.xml           # Benchmark config
└── META-INF/resources/
    └── index.html                # Web UI

src/test/java/.../benchmark/
└── RollingStockBenchmarkApp.java # Benchmark runner
```

## 👤 Autors

**Arturs** | LU EZTF DF Maģistrantūra  
Praktiskā Kombinatorika – Lielais MD  
2024/2025 studiju gads
