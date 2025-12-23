package org.acme.rollingstockrostering.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.rollingstockrostering.domain.RollingStockSchedule;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * RollingStockSchedulingResource - REST API for the Rolling Stock Rostering problem
 * 
 * Endpoints:
 * - GET /rolling-stock-schedule - Get demo schedule
 * - POST /rolling-stock-schedule/solve - Start solving
 * - GET /rolling-stock-schedule/stop-solving - Stop solving
 */
@Path("/rolling-stock-schedule")
@Produces(MediaType.APPLICATION_JSON)
public class RollingStockSchedulingResource {
    
    public static final Long SINGLETON_ID = 1L;
    
    @Inject
    SolverManager<RollingStockSchedule, Long> solverManager;
    
    @Inject
    DemoDataGenerator demoDataGenerator;
    
    // Store the best solution found by the solver
    private volatile RollingStockSchedule bestSolution = null;
    
    /**
     * GET /rolling-stock-schedule
     * Returns the current schedule
     */
    @GET
    public RollingStockSchedule getSchedule() {
        try {
            SolverStatus solverStatus = solverManager.getSolverStatus(SINGLETON_ID);
            
            // If we have a solution from the solver, return it
            // Otherwise return fresh demo data
            RollingStockSchedule schedule;
            if (bestSolution != null) {
                schedule = bestSolution;
            } else {
                schedule = demoDataGenerator.generateDemoData();
            }
            
            schedule.setSolverStatus(solverStatus);
            return schedule;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting schedule: " + e.getMessage(), e);
        }
    }
    
    /**
     * POST /rolling-stock-schedule/solve
     * Starts the solver to find optimal train assignments
     */
    @POST
    @Path("/solve")
    public void solve() {
        try {
            // Check if solver is already running - if so, stop it first
            SolverStatus status = solverManager.getSolverStatus(SINGLETON_ID);
            if (status == SolverStatus.SOLVING_ACTIVE || status == SolverStatus.SOLVING_SCHEDULED) {
                solverManager.terminateEarly(SINGLETON_ID);
                // Give it a moment to stop
                Thread.sleep(100);
            }
            
            // Reset best solution when starting new solve
            bestSolution = null;
            
            solverManager.solveBuilder()
                    .withProblemId(SINGLETON_ID)
                    .withProblemFinder(id -> demoDataGenerator.generateDemoData())
                    .withBestSolutionConsumer(solution -> bestSolution = solution)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting solver: " + e.getMessage(), e);
        }
    }
    
    /**
     * GET /rolling-stock-schedule/stop-solving
     * Stops the currently running solver
     */
    @GET
    @Path("/stop-solving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_ID);
    }
}
