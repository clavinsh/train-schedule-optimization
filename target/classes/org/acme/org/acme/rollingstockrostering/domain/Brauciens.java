package org.acme.rollingstockrostering.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Brauciens represents ONE complete trip on a route.
 * For example: "6:00 train from Rīga to Jelgava"
 * 
 * This is the planning entity - the solver assigns ONE train to each Brauciens.
 */
@PlanningEntity
public class Brauciens {
    
    @PlanningId
    private Long id;
    
    private Long marsrutaId;           // Which route
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime sakumaLaiks;     // Departure time from first station
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime beidzasLaiks;    // Arrival time at last station
    
    private Long sakumaStacijaId;      // First station ID
    private Long beidzasStacijaId;     // Last station ID
    private List<Long> stacijuIds;     // All stations in order
    private int kopejaisPasazieru;     // Total expected passengers for this trip
    
    // Detailed stop times: stationId -> arrival time (will use formatTime in frontend)
    private Map<Long, LocalTime> pieturuLaiki;
    
    @PlanningVariable(valueRangeProviderRefs = "vilcienuRange")
    private Vilciens vilciens;         // The train assigned to this trip

    public Brauciens() {}

    public Brauciens(Long id, Long marsrutaId, LocalTime sakumaLaiks, LocalTime beidzasLaiks,
                     Long sakumaStacijaId, Long beidzasStacijaId, 
                     List<Long> stacijuIds, int kopejaisPasazieru) {
        this.id = id;
        this.marsrutaId = marsrutaId;
        this.sakumaLaiks = sakumaLaiks;
        this.beidzasLaiks = beidzasLaiks;
        this.sakumaStacijaId = sakumaStacijaId;
        this.beidzasStacijaId = beidzasStacijaId;
        this.stacijuIds = stacijuIds;
        this.kopejaisPasazieru = kopejaisPasazieru;
    }
    
    public Brauciens(Long id, Long marsrutaId, LocalTime sakumaLaiks, LocalTime beidzasLaiks,
                     Long sakumaStacijaId, Long beidzasStacijaId, 
                     List<Long> stacijuIds, int kopejaisPasazieru,
                     Map<Long, LocalTime> pieturuLaiki) {
        this(id, marsrutaId, sakumaLaiks, beidzasLaiks, sakumaStacijaId, beidzasStacijaId, stacijuIds, kopejaisPasazieru);
        this.pieturuLaiki = pieturuLaiki;
    }

    // Check if this trip overlaps in time with another trip
    public boolean overlapsInTime(Brauciens other) {
        // Two trips overlap if one starts before the other ends
        return !(this.beidzasLaiks.isBefore(other.sakumaLaiks) || 
                 other.beidzasLaiks.isBefore(this.sakumaLaiks));
    }
    
    // Check if a train can transition from this trip to the next
    // Train MUST end where the next trip starts - no deadheading allowed
    public boolean canTransitionTo(Brauciens next) {
        // This trip must end before next starts
        if (!this.beidzasLaiks.isBefore(next.sakumaLaiks)) {
            return false;
        }
        
        // Train must end at the same station where next trip starts
        // This ensures realistic continuous operation
        return this.beidzasStacijaId.equals(next.sakumaStacijaId);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMarsrutaId() { return marsrutaId; }
    public void setMarsrutaId(Long marsrutaId) { this.marsrutaId = marsrutaId; }
    public LocalTime getSakumaLaiks() { return sakumaLaiks; }
    public void setSakumaLaiks(LocalTime sakumaLaiks) { this.sakumaLaiks = sakumaLaiks; }
    public LocalTime getBeidzasLaiks() { return beidzasLaiks; }
    public void setBeidzasLaiks(LocalTime beidzasLaiks) { this.beidzasLaiks = beidzasLaiks; }
    public Long getSakumaStacijaId() { return sakumaStacijaId; }
    public void setSakumaStacijaId(Long sakumaStacijaId) { this.sakumaStacijaId = sakumaStacijaId; }
    public Long getBeidzasStacijaId() { return beidzasStacijaId; }
    public void setBeidzasStacijaId(Long beidzasStacijaId) { this.beidzasStacijaId = beidzasStacijaId; }
    public List<Long> getStacijuIds() { return stacijuIds; }
    public void setStacijuIds(List<Long> stacijuIds) { this.stacijuIds = stacijuIds; }
    public int getKopejaisPasazieru() { return kopejaisPasazieru; }
    public void setKopejaisPasazieru(int kopejaisPasazieru) { this.kopejaisPasazieru = kopejaisPasazieru; }
    public Vilciens getVilciens() { return vilciens; }
    public void setVilciens(Vilciens vilciens) { this.vilciens = vilciens; }
    public Map<Long, LocalTime> getPieturuLaiki() { return pieturuLaiki; }
    public void setPieturuLaiki(Map<Long, LocalTime> pieturuLaiki) { this.pieturuLaiki = pieturuLaiki; }
    
    @Override
    public String toString() {
        return "Brauciens{" + id + ", route=" + marsrutaId + 
               ", " + sakumaLaiks + "-" + beidzasLaiks + 
               ", train=" + (vilciens != null ? vilciens.getId() : "null") + "}";
    }
}
