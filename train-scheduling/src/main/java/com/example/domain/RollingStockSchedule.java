package com.example.domain;

import java.time.LocalTime;
import java.util.List;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The planning solution for rolling stock scheduling optimization.
 * Contains all problem facts (input data) and planning entities (what Timefold optimizes).
 */
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@PlanningSolution
public class RollingStockSchedule {
    // Problem facts - input data that doesn't change during solving
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "trainRange")
    private List<Train> trains;

    @ProblemFactCollectionProperty
    private List<Station> stations;

    @ProblemFactCollectionProperty
    private List<Route> routes;

    @ProblemFactCollectionProperty
    private List<Depo> depos;

    @ProblemFactCollectionProperty
    private List<TrainDepoAssignment> trainDepoAssignments;

    @ProblemFactCollectionProperty
    private List<StationDemand> stationDemands;

    @ProblemFactProperty
    private TrainConfiguration configuration;

    // Value range for departure times (generated list of possible times)
    @ValueRangeProvider(id = "timeRange")
    @ProblemFactCollectionProperty
    private List<LocalTime> availableDepartureTimes;

    // Planning entities - what Timefold optimizes (each represents a potential train trip)
    @PlanningEntityCollectionProperty
    private List<RouteDeparture> routeDepartures;

    // The score calculated by the constraint provider
    @PlanningScore
    private HardSoftScore score;

    // Transient field for JSON serialization only (not part of planning, computed on-demand)
    @JsonIgnore
    private transient SolverStatus solverStatus;

    @JsonProperty("solverStatus")
    public String getSolverStatusString() {
        return solverStatus != null ? solverStatus.name() : "NOT_SOLVING";
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
