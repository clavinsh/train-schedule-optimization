package org.acme.rollingstockrostering.domain;

/**
 * Depo (Depot) - Problem Fact
 * 
 * Represents a train depot where trains must start and end their routes.
 * Used in the "trainEndsAtDepot" hard constraint.
 */
public class Depo {
    
    private Long id;
    private Long vilciensId; // Associated train ID
    private Long stacijaId; // Station ID where depot is located
    
    public Depo() {
    }
    
    public Depo(Long id, Long vilciensId, Long stacijaId) {
        this.id = id;
        this.vilciensId = vilciensId;
        this.stacijaId = stacijaId;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVilciensId() {
        return vilciensId;
    }
    
    public void setVilciensId(Long vilciensId) {
        this.vilciensId = vilciensId;
    }
    
    public Long getStacijaId() {
        return stacijaId;
    }
    
    public void setStacijaId(Long stacijaId) {
        this.stacijaId = stacijaId;
    }
    
    @Override
    public String toString() {
        return "Depo{" +
                "id=" + id +
                ", vilciensId=" + vilciensId +
                ", stacijaId=" + stacijaId +
                '}';
    }
}
