package org.acme.rollingstockrostering.rest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.rollingstockrostering.domain.RollingStockSchedule;

@Path("/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @Inject
    SolverManager<RollingStockSchedule, String> solverManager;

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
}
