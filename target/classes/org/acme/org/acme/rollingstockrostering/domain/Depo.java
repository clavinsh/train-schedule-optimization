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
