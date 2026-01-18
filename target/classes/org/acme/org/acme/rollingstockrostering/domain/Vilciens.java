package org.acme.rollingstockrostering.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Vilciens {
    @PlanningId
    private Long id;
    private int kapacitate;
    private Long depoStacijaId;  // Where this train starts and ends the day

    public Vilciens() {}

    public Vilciens(Long id, int kapacitate) {
        this.id = id;
        this.kapacitate = kapacitate;
    }
    
    public Vilciens(Long id, int kapacitate, Long depoStacijaId) {
        this.id = id;
        this.kapacitate = kapacitate;
        this.depoStacijaId = depoStacijaId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getKapacitate() { return kapacitate; }
    public void setKapacitate(int kapacitate) { this.kapacitate = kapacitate; }
    public Long getDepoStacijaId() { return depoStacijaId; }
    public void setDepoStacijaId(Long depoStacijaId) { this.depoStacijaId = depoStacijaId; }
    
    // Helper method to get display name
    public String getNosaukums() {
        return "V" + id;
    }
}
