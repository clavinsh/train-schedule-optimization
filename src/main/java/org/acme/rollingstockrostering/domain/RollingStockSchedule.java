package org.acme.rollingstockrostering.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * RollingStockSchedule - PLANNING SOLUTION ⭐
 * 
 * This is the main solution class that contains:
 * 1. Problem facts (input data that doesn't change)
 * 2. Planning entities (data that Timefold will optimize)
 * 3. The score (how good the solution is)
 * 
 * TIMEFOLD ANNOTATIONS EXPLAINED:
 * 
 * @PlanningSolution - Tells Timefold this is the solution class that wraps
 *                     the entire problem. One instance represents one solution.
 * 
 * @ProblemFactCollectionProperty - Marks collections of problem facts (input data).
 *                                  These are read-only during solving.
 *                                  Examples: vilcieni, stacijas, marsruti
 * 
 * @ValueRangeProvider - Provides the range of values that can be assigned to
 *                       planning variables. The id "vilcienuRange" is referenced
 *                       by @PlanningVariable in AtiesanasLaiks.
 *                       Timefold will pick values from this list when assigning
 *                       trains to departure times.
 * 
 * @PlanningEntityCollectionProperty - Marks the collection of planning entities.
 *                                     These are the objects Timefold will modify
 *                                     during solving (by changing vilciensId).
 * 
 * @PlanningScore - The score field that Timefold updates during solving.
 *                  HardSoftScore has two components:
 *                  - Hard score: Must be 0 for a feasible solution
 *                  - Soft score: Higher is better (optimization objective)
 */
@PlanningSolution
public class RollingStockSchedule {
    
    /**
     * Problem Facts - Input data (read-only during solving)
     */
    @ProblemFactCollectionProperty
    private List<Stacija> stacijas;
    
    @ProblemFactCollectionProperty
    private List<Marsruts> marsruti;
    
    @ProblemFactCollectionProperty
    private List<Depo> depo;
    
    @ProblemFactCollectionProperty
    private List<CilvekuPieprasijums> cilvekuPieprasijumi;
    
    private Konfiguracija konfiguracija; // Single problem fact (not a collection)
    
    /**
     * Value Range Provider - Available trains that can be assigned
     * 
     * The id "vilcienuRange" is referenced in @PlanningVariable of AtiesanasLaiks.
     * When Timefold assigns a vilciensId, it picks from the IDs in this list.
     * 
     * NOTE: We return the List<Vilciens> objects directly. Timefold will extract
     * the IDs when assigning to Long vilciensId in AtiesanasLaiks.
     */
    @ValueRangeProvider(id = "vilcienuRange")
    @ProblemFactCollectionProperty
    private List<Vilciens> vilcieni;
    
    /**
     * Planning Entities - The objects Timefold will optimize
     * 
     * Each AtiesanasLaiks has a vilciensId that starts as null.
     * Timefold will assign trains to these departure times to find
     * the best schedule that satisfies constraints.
     */
    @PlanningEntityCollectionProperty
    private List<AtiesanasLaiks> atiesanasLaiki;
    
    /**
     * Planning Score - Measures solution quality
     * 
     * HardSoftScore components:
     * - Hard score: Sum of hard constraint violations (must be 0)
     *   Examples: train visits all stations, capacity not exceeded
     * 
     * - Soft score: Sum of soft constraint penalties/rewards
     *   Examples: minimize delays, maximize passenger pickup
     * 
     * Timefold tries to find a solution with hard score = 0 and
     * maximum soft score.
     */
    @PlanningScore
    private HardSoftScore score;
    
    // Solver status (not used by Timefold, but useful for REST API)
    private SolverStatus solverStatus;
    
    // No-arg constructor required by Timefold
    public RollingStockSchedule() {
    }
    
    public RollingStockSchedule(List<Vilciens> vilcieni, 
                                List<Stacija> stacijas,
                                List<Marsruts> marsruti, 
                                List<Depo> depo,
                                List<CilvekuPieprasijums> cilvekuPieprasijumi,
                                Konfiguracija konfiguracija,
                                List<AtiesanasLaiks> atiesanasLaiki) {
        this.vilcieni = vilcieni;
        this.stacijas = stacijas;
        this.marsruti = marsruti;
        this.depo = depo;
        this.cilvekuPieprasijumi = cilvekuPieprasijumi;
        this.konfiguracija = konfiguracija;
        this.atiesanasLaiki = atiesanasLaiki;
    }
    
    // Getters and setters
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
    
    public List<Depo> getDepo() {
        return depo;
    }
    
    public void setDepo(List<Depo> depo) {
        this.depo = depo;
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
    
    public List<AtiesanasLaiks> getAtiesanasLaiki() {
        return atiesanasLaiki;
    }
    
    public void setAtiesanasLaiki(List<AtiesanasLaiks> atiesanasLaiki) {
        this.atiesanasLaiki = atiesanasLaiki;
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
}
