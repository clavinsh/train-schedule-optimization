package org.acme.rollingstockrostering.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * RollingStockSchedule - PLANNING SOLUTION
 *
 * This is the main solution class that contains:
 * 1. Problem facts (input data that doesn't change during solving)
 * 2. Planning entities (data that Timefold will optimize)
 * 3. The score (how good the solution is)
 *
 * The solver assigns trains (Vilciens) to trips (Trip) to optimize the schedule.
 *
 * TIMEFOLD ANNOTATIONS:
 * @PlanningSolution - Identifies this as the solution class
 * @ProblemFactCollectionProperty - Marks immutable input data collections
 * @ProblemFactProperty - Marks single immutable input data
 * @ValueRangeProvider - Provides values for planning variables
 * @PlanningEntityCollectionProperty - Marks entities to be optimized
 * @PlanningScore - The solution quality measure
 */
@PlanningSolution
public class RollingStockSchedule {

    // ==================== PROBLEM FACTS ====================

    /**
     * Available trains with their capacities.
     * This also serves as the value range for the planning variable.
     */
    @ValueRangeProvider(id = "vilcienuRange")
    @ProblemFactCollectionProperty
    private List<Vilciens> vilcieni;

    /**
     * Railway stations with locations and connections.
     */
    @ProblemFactCollectionProperty
    private List<Stacija> stacijas;

    /**
     * Routes defining sequences of stations.
     */
    @ProblemFactCollectionProperty
    private List<Marsruts> marsruti;

    /**
     * Depot locations where trains are stored.
     */
    @ProblemFactCollectionProperty
    private List<Depo> depiList;

    /**
     * Train-to-depot assignments (which train belongs to which depot).
     */
    @ProblemFactCollectionProperty
    private List<TrainDepotAssignment> trainDepotAssignments;

    /**
     * Passenger demand data by station, route, and time.
     */
    @ProblemFactCollectionProperty
    private List<CilvekuPieprasijums> cilvekuPieprasijumi;

    /**
     * Global configuration parameters.
     */
    @ProblemFactProperty
    private Konfiguracija konfiguracija;

    // ==================== PLANNING ENTITIES ====================

    /**
     * Trips to be optimized - each trip needs a train assigned.
     * This is the main planning entity collection.
     */
    @PlanningEntityCollectionProperty
    private List<Trip> trips;

    // ==================== SCORE ====================

    /**
     * The solution quality score.
     * Hard score must be 0 for feasibility, soft score is maximized.
     */
    @PlanningScore
    private HardSoftScore score;

    /**
     * Solver status for REST API integration.
     */
    private SolverStatus solverStatus;

    // ==================== CONSTRUCTORS ====================

    public RollingStockSchedule() {
        this.vilcieni = new ArrayList<>();
        this.stacijas = new ArrayList<>();
        this.marsruti = new ArrayList<>();
        this.depiList = new ArrayList<>();
        this.trainDepotAssignments = new ArrayList<>();
        this.cilvekuPieprasijumi = new ArrayList<>();
        this.trips = new ArrayList<>();
        this.konfiguracija = new Konfiguracija();
    }

    public RollingStockSchedule(List<Vilciens> vilcieni,
                                List<Stacija> stacijas,
                                List<Marsruts> marsruti,
                                List<Depo> depiList,
                                List<TrainDepotAssignment> trainDepotAssignments,
                                List<CilvekuPieprasijums> cilvekuPieprasijumi,
                                Konfiguracija konfiguracija,
                                List<Trip> trips) {
        this.vilcieni = vilcieni != null ? vilcieni : new ArrayList<>();
        this.stacijas = stacijas != null ? stacijas : new ArrayList<>();
        this.marsruti = marsruti != null ? marsruti : new ArrayList<>();
        this.depiList = depiList != null ? depiList : new ArrayList<>();
        this.trainDepotAssignments = trainDepotAssignments != null ? trainDepotAssignments : new ArrayList<>();
        this.cilvekuPieprasijumi = cilvekuPieprasijumi != null ? cilvekuPieprasijumi : new ArrayList<>();
        this.konfiguracija = konfiguracija != null ? konfiguracija : new Konfiguracija();
        this.trips = trips != null ? trips : new ArrayList<>();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get a map of routes by ID for quick lookup.
     */
    public Map<Long, Marsruts> getRouteMap() {
        return marsruti.stream()
                .collect(Collectors.toMap(Marsruts::getId, Function.identity()));
    }

    /**
     * Get a map of stations by ID for quick lookup.
     */
    public Map<Long, Stacija> getStationMap() {
        return stacijas.stream()
                .collect(Collectors.toMap(Stacija::getId, Function.identity()));
    }

    /**
     * Get a map of depots by station ID for quick lookup.
     */
    public Map<Long, Depo> getDepotByStationMap() {
        return depiList.stream()
                .collect(Collectors.toMap(Depo::getStacijaId, Function.identity(), (a, b) -> a));
    }

    /**
     * Get the depot assigned to a specific train.
     */
    public Depo getDepotForTrain(Long trainId) {
        return trainDepotAssignments.stream()
                .filter(tda -> tda.getVilciensId().equals(trainId))
                .findFirst()
                .map(tda -> depiList.stream()
                        .filter(d -> d.getId().equals(tda.getDepoId()))
                        .findFirst()
                        .orElse(null))
                .orElse(null);
    }

    /**
     * Get the route for a trip.
     */
    public Marsruts getRouteForTrip(Trip trip) {
        if (trip == null || trip.getMarsrutaId() == null) {
            return null;
        }
        return marsruti.stream()
                .filter(m -> m.getId().equals(trip.getMarsrutaId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the first station of a trip (considering direction).
     */
    public Long getFirstStationIdForTrip(Trip trip) {
        Marsruts route = getRouteForTrip(trip);
        if (route == null) {
            return null;
        }
        return trip.isReturnTrip() ? route.getLastStationId() : route.getFirstStationId();
    }

    /**
     * Get the last station of a trip (considering direction).
     */
    public Long getLastStationIdForTrip(Trip trip) {
        Marsruts route = getRouteForTrip(trip);
        if (route == null) {
            return null;
        }
        return trip.isReturnTrip() ? route.getFirstStationId() : route.getLastStationId();
    }

    // ==================== GETTERS AND SETTERS ====================

    public List<Vilciens> getVilcieni() {
        return vilcieni;
    }

    public void setVilcieni(List<Vilciens> vilcieni) {
        this.vilcieni = vilcieni;
    }

    public List<Stacija> getStacijas() {
        return stacijas;
    }

    public void setStacijas(List<Stacija> stacijas) {
        this.stacijas = stacijas;
    }

    public List<Marsruts> getMarsruti() {
        return marsruti;
    }

    public void setMarsruti(List<Marsruts> marsruti) {
        this.marsruti = marsruti;
    }

    public List<Depo> getDepiList() {
        return depiList;
    }

    public void setDepiList(List<Depo> depiList) {
        this.depiList = depiList;
    }

    // Backward compatibility alias
    public List<Depo> getDepo() {
        return depiList;
    }

    public void setDepo(List<Depo> depo) {
        this.depiList = depo;
    }

    public List<TrainDepotAssignment> getTrainDepotAssignments() {
        return trainDepotAssignments;
    }

    public void setTrainDepotAssignments(List<TrainDepotAssignment> trainDepotAssignments) {
        this.trainDepotAssignments = trainDepotAssignments;
    }

    public List<CilvekuPieprasijums> getCilvekuPieprasijumi() {
        return cilvekuPieprasijumi;
    }

    public void setCilvekuPieprasijumi(List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        this.cilvekuPieprasijumi = cilvekuPieprasijumi;
    }

    public Konfiguracija getKonfiguracija() {
        return konfiguracija;
    }

    public void setKonfiguracija(Konfiguracija konfiguracija) {
        this.konfiguracija = konfiguracija;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    // ==================== BACKWARD COMPATIBILITY ====================

    /**
     * @deprecated Use getTrips() instead
     */
    @Deprecated
    public List<AtiesanasLaiks> getAtiesanasLaiki() {
        // Return empty list for backward compatibility
        return new ArrayList<>();
    }

    /**
     * @deprecated Use setTrips() instead
     */
    @Deprecated
    public void setAtiesanasLaiki(List<AtiesanasLaiks> atiesanasLaiki) {
        // No-op for backward compatibility
    }
}
