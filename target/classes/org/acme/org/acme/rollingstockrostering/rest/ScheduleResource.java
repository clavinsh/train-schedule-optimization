package org.acme.rollingstockrostering.rest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.ScoreManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.rollingstockrostering.domain.RollingStockSchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @Inject
    SolverManager<RollingStockSchedule, String> solverManager;

    @Inject
    ScoreManager<RollingStockSchedule, HardSoftScore> scoreManager;

    @Inject
    DemoDataGenerator demoDataGenerator;

    private ConcurrentMap<String, RollingStockSchedule> scheduleMap = new ConcurrentHashMap<>();
    private String currentJobId = null;

    @GET
    public RollingStockSchedule getSchedule() {
        if (currentJobId == null) {
            return demoDataGenerator.generateDemoData();
        }
        RollingStockSchedule schedule = scheduleMap.getOrDefault(currentJobId, demoDataGenerator.generateDemoData());
        schedule.setSolverStatus(solverManager.getSolverStatus(currentJobId));
        return schedule;
    }

    @POST
    @Path("/solve")
    public void solve() {
        currentJobId = UUID.randomUUID().toString();
        RollingStockSchedule problem = demoDataGenerator.generateDemoData();
        scheduleMap.put(currentJobId, problem);
        
        solverManager.solveBuilder()
            .withProblemId(currentJobId)
            .withProblemFinder(id -> scheduleMap.get(id))
            .withBestSolutionConsumer(solution -> scheduleMap.put(currentJobId, solution))
            .run();
    }

    @POST
    @Path("/stop")
    public void stop() {
        if (currentJobId != null) {
            solverManager.terminateEarly(currentJobId);
        }
    }

    @GET
    @Path("/status")
    public SolverStatus getStatus() {
        if (currentJobId == null) {
            return SolverStatus.NOT_SOLVING;
        }
        return solverManager.getSolverStatus(currentJobId);
    }
    
    @POST
    @Path("/generate")
    public RollingStockSchedule generateData(
            @QueryParam("trains") Integer trainCount,
            @QueryParam("startHour") Integer startHour,
            @QueryParam("endHour") Integer endHour,
            @QueryParam("interval") Integer interval) {
        
        // Use defaults if not provided
        int trains = trainCount != null ? trainCount : 5;
        int start = startHour != null ? startHour : 8;
        int end = endHour != null ? endHour : 18;
        int intervalHours = interval != null ? interval : 3;
        
        RollingStockSchedule schedule = demoDataGenerator.generateDataset(trains, start, end, intervalHours);
        
        // Clear current job if exists
        if (currentJobId != null) {
            solverManager.terminateEarly(currentJobId);
            currentJobId = null;
        }
        
        return schedule;
    }
    
    @PUT
    @Path("/analyze")
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> analyzeScore(RollingStockSchedule schedule) {
        ScoreExplanation<RollingStockSchedule, HardSoftScore> explanation = 
            scoreManager.explainScore(schedule);
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> constraints = new ArrayList<>();
        
        for (ConstraintMatchTotal<HardSoftScore> constraintMatchTotal : explanation.getConstraintMatchTotalMap().values()) {
            Map<String, Object> constraintInfo = new HashMap<>();
            constraintInfo.put("constraintName", constraintMatchTotal.getConstraintName());
            constraintInfo.put("constraintPackage", constraintMatchTotal.getConstraintPackage());
            constraintInfo.put("score", constraintMatchTotal.getScore().toString());
            constraintInfo.put("constraintMatchCount", constraintMatchTotal.getConstraintMatchCount());
            
            HardSoftScore score = constraintMatchTotal.getScore();
            if (!score.isZero()) {
                constraints.add(constraintInfo);
            }
        }
        
        result.put("constraints", constraints);
        result.put("score", schedule.getScore().toString());
        return result;
    }
}
