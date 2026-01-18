package org.acme.rollingstockrostering.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class RollingStockSchedule {
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "vilcienuRange")
    private List<Vilciens> vilcieni;
    
    @ProblemFactCollectionProperty
    private List<Stacija> stacijas;
    
    @ProblemFactCollectionProperty
    private List<Marsruts> marsruti;
    
    @ProblemFactCollectionProperty
    private List<Depo> depiList;
    
    @ProblemFactCollectionProperty
    private List<CilvekuPieprasijums> cilvekuPieprasijumi;
    
    @ProblemFactProperty
    private Konfiguracija konfiguracija;
    
    @PlanningEntityCollectionProperty
    private List<Brauciens> braucieni;
    
    @PlanningScore
    private HardSoftScore score;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    public RollingStockSchedule() {}

    public RollingStockSchedule(List<Vilciens> vilcieni, List<Stacija> stacijas,
                                List<Marsruts> marsruti, List<Depo> depiList,
                                List<CilvekuPieprasijums> cilvekuPieprasijumi,
                                Konfiguracija konfiguracija,
                                List<Brauciens> braucieni) {
        this.vilcieni = vilcieni;
        this.stacijas = stacijas;
        this.marsruti = marsruti;
        this.depiList = depiList;
        this.cilvekuPieprasijumi = cilvekuPieprasijumi;
        this.konfiguracija = konfiguracija;
        this.braucieni = braucieni;
    }

    public RollingStockSchedule(HardSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    // Getters and setters
    public List<Vilciens> getVilcieni() { return vilcieni; }
    public void setVilcieni(List<Vilciens> vilcieni) { this.vilcieni = vilcieni; }
    public List<Stacija> getStacijas() { return stacijas; }
    public void setStacijas(List<Stacija> stacijas) { this.stacijas = stacijas; }
    public List<Marsruts> getMarsruti() { return marsruti; }
    public void setMarsruti(List<Marsruts> marsruti) { this.marsruti = marsruti; }
    public List<Depo> getDepiList() { return depiList; }
    public void setDepiList(List<Depo> depiList) { this.depiList = depiList; }
    public List<CilvekuPieprasijums> getCilvekuPieprasijumi() { return cilvekuPieprasijumi; }
    public void setCilvekuPieprasijumi(List<CilvekuPieprasijums> cilvekuPieprasijumi) { this.cilvekuPieprasijumi = cilvekuPieprasijumi; }
    public Konfiguracija getKonfiguracija() { return konfiguracija; }
    public void setKonfiguracija(Konfiguracija konfiguracija) { this.konfiguracija = konfiguracija; }
    public List<Brauciens> getBraucieni() { return braucieni; }
    public void setBraucieni(List<Brauciens> braucieni) { this.braucieni = braucieni; }
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }
    public SolverStatus getSolverStatus() { return solverStatus; }
    public void setSolverStatus(SolverStatus solverStatus) { this.solverStatus = solverStatus; }
}
