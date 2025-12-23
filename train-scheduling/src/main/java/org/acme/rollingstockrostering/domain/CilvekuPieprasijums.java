package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;

/**
 * CilvēkuPieprasījums (PassengerDemand) - Problem Fact
 * 
 * Represents passenger demand at a specific station, route, and time.
 * Used in the "maximizePassengerPickup" soft constraint to reward 
 * train schedules that align with passenger demand.
 */
public class CilvekuPieprasijums {
    
    private Long id;
    private Long stacijasId; // Station ID
    private Long marsrutaId; // Route ID
    private LocalTime stunda; // Hour of demand
    private int cilvekuSkaits; // Number of passengers
    
    public CilvekuPieprasijums() {
    }
    
    public CilvekuPieprasijums(Long id, Long stacijasId, Long marsrutaId, 
                                LocalTime stunda, int cilvekuSkaits) {
        this.id = id;
        this.stacijasId = stacijasId;
        this.marsrutaId = marsrutaId;
        this.stunda = stunda;
        this.cilvekuSkaits = cilvekuSkaits;
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
    
    public LocalTime getStunda() {
        return stunda;
    }
    
    public void setStunda(LocalTime stunda) {
        this.stunda = stunda;
    }
    
    public int getCilvekuSkaits() {
        return cilvekuSkaits;
    }
    
    public void setCilvekuSkaits(int cilvekuSkaits) {
        this.cilvekuSkaits = cilvekuSkaits;
    }
    
    @Override
    public String toString() {
        return "CilvekuPieprasijums{" +
                "id=" + id +
                ", stacijasId=" + stacijasId +
                ", marsrutaId=" + marsrutaId +
                ", stunda=" + stunda +
                ", cilvekuSkaits=" + cilvekuSkaits +
                '}';
    }
}
