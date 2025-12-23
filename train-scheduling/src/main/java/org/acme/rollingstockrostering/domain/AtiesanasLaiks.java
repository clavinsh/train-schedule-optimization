package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * AtiešanasLaiks (DepartureTime) - PLANNING ENTITY ⭐
 * 
 * This is the core planning entity that Timefold will optimize.
 * Each instance represents a departure time at a specific station on a route.
 * 
 * TIMEFOLD ANNOTATIONS EXPLAINED:
 * 
 * @PlanningEntity - Tells Timefold this class represents entities to be optimized.
 *                   Timefold will create instances of this class and assign values
 *                   to its planning variables to find the best solution.
 * 
 * @PlanningId - Uniquely identifies this entity. Required for Timefold to track
 *              entities during solving. Must be unique across all planning entities.
 * 
 * @PlanningVariable - Marks the field that Timefold will assign values to.
 *                     In our case: vilciensId (which train is assigned to this departure).
 *                     valueRangeProviderRefs points to the "vilcienuRange" in the 
 *                     @PlanningSolution class, which provides the list of available trains.
 * 
 * The planning variable can be NULL initially (unassigned state), and Timefold
 * will try different combinations of train assignments to minimize constraint violations.
 */
@PlanningEntity
public class AtiesanasLaiks {
    
    @PlanningId
    private Long id;
    
    private Long stacijasId; // Station ID (problem fact)
    private Long marsrutaId; // Route ID (problem fact)
    private LocalTime laiks; // Departure time (problem fact)
    private int cilvekuDelta; // Passengers picked up (problem fact)
    
    /**
     * PLANNING VARIABLE - This is what Timefold optimizes!
     * 
     * vilciens will be assigned by Timefold from the "vilcienuRange" 
     * (defined in RollingStockSchedule).
     * 
     * Timefold will try different train assignments to satisfy hard constraints:
     * - Train visits all stations on route
     * - Train capacity not exceeded
     * - Train ends at depot
     * 
     * And optimize soft constraints:
     * - Minimize delays
     * - Minimize empty trains
     * - Maximize passenger pickup
     */
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange")
    private Vilciens vilciens;
    
    // No-arg constructor required by Timefold
    public AtiesanasLaiks() {
    }
    
    public AtiesanasLaiks(Long id, Long stacijasId, Long marsrutaId, 
                          LocalTime laiks, int cilvekuDelta) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.laiks = laiks;
        this.cilvekuDelta = cilvekuDelta;
        // vilciens starts as null (unassigned)
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getStacijasId() {
        return stacijasId;
    }
    
    public void setStacijasId(Long stacijasId) {
        this.stacijasId = stacijasId;
    }
    
    public Long getMarsrutaId() {
        return marsrutaId;
    }
    
    public void setMarsrutaId(Long marsrutaId) {
        this.marsrutaId = marsrutaId;
    }
    
    public LocalTime getLaiks() {
        return laiks;
    }
    
    public void setLaiks(LocalTime laiks) {
        this.laiks = laiks;
    }
    
    public int getCilvekuDelta() {
        return cilvekuDelta;
    }
    
    public void setCilvekuDelta(int cilvekuDelta) {
        this.cilvekuDelta = cilvekuDelta;
    }
    
    public Vilciens getVilciens() {
        return vilciens;
    }
    
    public void setVilciens(Vilciens vilciens) {
        this.vilciens = vilciens;
    }
    
    /**
     * Helper method to get vilciens ID (for convenience)
     */
    public Long getVilciensId() {
        return vilciens == null ? null : vilciens.getId();
    }
    
    @Override
    public String toString() {
        return "AtiesanasLaiks{" +
                "id=" + id +
                ", stacijasId=" + stacijasId +
                ", marsrutaId=" + marsrutaId +
                ", laiks=" + laiks +
                ", vilciensId=" + getVilciensId() +
                ", cilvekuDelta=" + cilvekuDelta +
                '}';
    }
}
