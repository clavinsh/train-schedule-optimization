package org.acme.RollingStockRosteringOptimization.rest;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.RollingStockRosteringOptimization.domain.RollingStockSchedule;

import org.acme.RollingStockRosteringOptimization.domain.Ride;
import org.acme.RollingStockRosteringOptimization.domain.Route;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/rolling-stock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RollingStockSchedulingResource {

    private static final Logger LOG = LoggerFactory.getLogger(RollingStockSchedulingResource.class);

    private final SolverManager<RollingStockSchedule, String> solverManager;
    private final SolutionManager<RollingStockSchedule, HardSoftLongScore> solutionManager;
    private final DemoDataGenerator demoDataGenerator;

    // Store solutions by job ID
    private final ConcurrentMap<String, RollingStockSchedule> solutionMap = new ConcurrentHashMap<>();

    @Inject
    public RollingStockSchedulingResource(
            SolverManager<RollingStockSchedule, String> solverManager,
            SolutionManager<RollingStockSchedule, HardSoftLongScore> solutionManager,
            DemoDataGenerator demoDataGenerator) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
        this.demoDataGenerator = demoDataGenerator;
    }

    /**
     * Get demo data (unsolved schedule).
     */
    @GET
    @Path("/demo-data")
    public RollingStockSchedule getDemoData() {
        RollingStockSchedule schedule = demoDataGenerator.generateDemoData();
        schedule.setSolverStatus(SolverStatus.NOT_SOLVING);
        return schedule;
    }

    /**
     * List all job IDs.
     */
    @GET
    public Collection<String> listJobs() {
        return solutionMap.keySet();
    }

    /**
     * Submit a schedule for solving.
     * Returns the job ID that can be used to track progress.
     */
    @POST
    @SuppressWarnings("removal")
    public String solve(RollingStockSchedule problem) {
        String jobId = UUID.randomUUID().toString();
        solutionMap.put(jobId, problem);

        // Use solveAndListen to get intermediate best solutions for real-time updates
        solverManager.solveAndListen(jobId,
                id -> solutionMap.get(id),
                solution -> {
                    // Called on each new best solution
                    solution.setSolverStatus(solverManager.getSolverStatus(jobId));
                    // Calculate passenger counts based on assigned rides and demand
                    solution.calculatePassengerCounts();
                    solutionMap.put(jobId, solution);
                });

        return jobId;
    }

    /**
     * Get the current solution for a job.
     */
    @GET
    @Path("/{jobId}")
    public Response getSolution(@PathParam("jobId") String jobId) {
        RollingStockSchedule solution = solutionMap.get(jobId);
        if (solution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo("Job not found: " + jobId))
                    .build();
        }

        // Update solver status
        SolverStatus status = solverManager.getSolverStatus(jobId);
        solution.setSolverStatus(status);

        // Calculate passenger counts based on assigned rides and demand
        solution.calculatePassengerCounts();

        return Response.ok(solution).build();
    }

    /**
     * Get the current status and score for a job.
     */
    @GET
    @Path("/{jobId}/status")
    public Response getStatus(@PathParam("jobId") String jobId) {
        RollingStockSchedule solution = solutionMap.get(jobId);
        if (solution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo("Job not found: " + jobId))
                    .build();
        }

        SolverStatus status = solverManager.getSolverStatus(jobId);
        return Response.ok(new JobStatus(jobId, status, solution.getScore())).build();
    }

    /**
     * Analyze the score of a schedule without solving.
     * Uses explain() to get detailed constraint match information.
     */
    @PUT
    @Path("/analyze")
    public ScoreAnalysisResponse analyze(RollingStockSchedule schedule) {
        ScoreExplanation<RollingStockSchedule, HardSoftLongScore> explanation = solutionManager.explain(schedule);
        return ScoreAnalysisResponse.from(explanation);
    }

    /**
     * Get detailed constraint analysis for a job.
     */
    @GET
    @Path("/{jobId}/analysis")
    public Response getAnalysis(@PathParam("jobId") String jobId) {
        RollingStockSchedule solution = solutionMap.get(jobId);
        if (solution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo("Job not found: " + jobId))
                    .build();
        }

        ScoreExplanation<RollingStockSchedule, HardSoftLongScore> explanation = solutionManager.explain(solution);
        return Response.ok(ScoreAnalysisResponse.from(explanation)).build();
    }

    /**
     * Stop solving for a job.
     */
    @DELETE
    @Path("/{jobId}")
    public Response stopSolving(@PathParam("jobId") String jobId) {
        RollingStockSchedule solution = solutionMap.get(jobId);
        if (solution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo("Job not found: " + jobId))
                    .build();
        }

        solverManager.terminateEarly(jobId);
        return Response.ok().build();
    }

    /**
     * Delete a job and its solution.
     */
    @DELETE
    @Path("/{jobId}/delete")
    public Response deleteJob(@PathParam("jobId") String jobId) {
        solverManager.terminateEarly(jobId);
        RollingStockSchedule removed = solutionMap.remove(jobId);
        if (removed == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorInfo("Job not found: " + jobId))
                    .build();
        }
        return Response.ok().build();
    }

    // ========================================================================
    // Response DTOs
    // ========================================================================

    public record ErrorInfo(String message) {}

    public record JobStatus(
            String jobId,
            SolverStatus solverStatus,
            HardSoftLongScore score
    ) {}

    public record ScoreAnalysisResponse(
            String score,
            java.util.List<ConstraintAnalysisDTO> constraints
    ) {
        public static ScoreAnalysisResponse from(ScoreExplanation<RollingStockSchedule, HardSoftLongScore> explanation) {
            java.util.List<ConstraintAnalysisDTO> constraints = new java.util.ArrayList<>();

            // Get constraint match totals from explanation
            var constraintMatchTotals = explanation.getConstraintMatchTotalMap();
            for (var entry : constraintMatchTotals.entrySet()) {
                constraints.add(ConstraintAnalysisDTO.from(entry.getValue()));
            }

            return new ScoreAnalysisResponse(
                    explanation.getScore().toString(),
                    constraints
            );
        }
    }

    public record ConstraintAnalysisDTO(
            String name,
            String score,
            int matchCount,
            java.util.List<ConstraintMatchDTO> matches
    ) {
        public static ConstraintAnalysisDTO from(ConstraintMatchTotal<HardSoftLongScore> matchTotal) {
            java.util.List<ConstraintMatchDTO> matches = new java.util.ArrayList<>();

            var constraintMatches = matchTotal.getConstraintMatchSet();
            LOG.debug("Constraint '{}' has {} matches", matchTotal.getConstraintRef().constraintName(), constraintMatches.size());

            for (var cm : constraintMatches) {
                matches.add(ConstraintMatchDTO.from((ConstraintMatch<HardSoftLongScore>) cm));
            }

            return new ConstraintAnalysisDTO(
                    matchTotal.getConstraintRef().constraintName(),
                    matchTotal.getScore().toString(),
                    constraintMatches.size(),
                    matches
            );
        }
    }

    public record ConstraintMatchDTO(
            String score,
            java.util.List<String> rideIds,
            java.util.List<String> routeIds,
            String justification
    ) {
        public static ConstraintMatchDTO from(ConstraintMatch<HardSoftLongScore> match) {
            java.util.Set<String> rideIdSet = new java.util.LinkedHashSet<>();
            java.util.Set<String> routeIdSet = new java.util.LinkedHashSet<>();

            // Extract ride and route IDs from indicted objects (the facts that caused the match)
            var indictedObjects = match.getIndictedObjectList();
            if (indictedObjects != null) {
                for (Object obj : indictedObjects) {
                    if (obj instanceof Ride ride) {
                        rideIdSet.add(ride.getId());
                    } else if (obj instanceof Route route) {
                        routeIdSet.add(route.getId());
                    }
                }
            }

            // Build justification string
            String justificationStr = "";
            var justification = match.getJustification();
            if (justification != null) {
                justificationStr = justification.toString();

                // Try to extract from justification facts
                try {
                    var method = justification.getClass().getMethod("getFacts");
                    var facts = (java.util.Collection<?>) method.invoke(justification);
                    if (facts != null) {
                        for (Object fact : facts) {
                            if (fact instanceof Ride ride) {
                                rideIdSet.add(ride.getId());
                            } else if (fact instanceof Route route) {
                                routeIdSet.add(route.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }

                // Fallback: parse ride IDs from justification string
                if (rideIdSet.isEmpty()) {
                    java.util.regex.Pattern ridePattern = java.util.regex.Pattern.compile("([A-Za-z0-9_-]+)\\([^)]*->");
                    java.util.regex.Matcher matcher = ridePattern.matcher(justificationStr);
                    while (matcher.find()) {
                        String potentialId = matcher.group(1);
                        if (!potentialId.equalsIgnoreCase("Train") &&
                            !potentialId.equalsIgnoreCase("Route") &&
                            !potentialId.equalsIgnoreCase("Station") &&
                            !potentialId.equalsIgnoreCase("Demand") &&
                            !potentialId.equalsIgnoreCase("Depo")) {
                            rideIdSet.add(potentialId);
                        }
                    }
                }
            }

            LOG.trace("Match rideIds: {}, routeIds: {}, justification: {}", rideIdSet, routeIdSet, justificationStr);

            return new ConstraintMatchDTO(
                    match.getScore().toString(),
                    new java.util.ArrayList<>(rideIdSet),
                    new java.util.ArrayList<>(routeIdSet),
                    justificationStr
            );
        }
    }
}
