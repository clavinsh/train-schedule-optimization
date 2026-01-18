package com.example.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJobBuilder;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.domain.RollingStockSchedule;
import com.example.rest.exception.ErrorInfo;
import com.example.rest.exception.ScheduleSolverException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Tag(name = "Train Scheduling",
        description = "Train scheduling service for optimizing rolling stock assignments")
@Path("schedules")
public class TrainSchedulingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainSchedulingResource.class);

    @Inject
    SolverManager<RollingStockSchedule, String> solverManager;

    @Inject
    SolutionManager<RollingStockSchedule, HardSoftScore> solutionManager;

    // In-memory storage for schedules (in production, use a database)
    private final ConcurrentMap<String, RollingStockSchedule> scheduleMap =
            new ConcurrentHashMap<>();

    @Operation(summary = "List all schedule job IDs")
    @APIResponses(value = {@APIResponse(responseCode = "200",
            description = "List of schedule job IDs",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> list() {
        return scheduleMap.keySet();
    }

    @Operation(summary = "Get a schedule by ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The schedule",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RollingStockSchedule.class))),
            @APIResponse(responseCode = "404", description = "Schedule not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))})
    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RollingStockSchedule getSchedule(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        RollingStockSchedule schedule = scheduleMap.get(jobId);
        if (schedule == null) {
            throw new ScheduleSolverException(jobId,
                    "Schedule with jobId '" + jobId + "' not found.");
        }
        // Inject current solver status for frontend polling
        schedule.setSolverStatus(solverManager.getSolverStatus(jobId));
        return schedule;
    }

    @Operation(summary = "Get the solver status for a schedule")
    @APIResponses(value = {@APIResponse(responseCode = "200", description = "The solver status",
            content = @Content(mediaType = MediaType.TEXT_PLAIN))})
    @GET
    @Path("{jobId}/status")
    @Produces(MediaType.TEXT_PLAIN)
    public SolverStatus getStatus(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        return solverManager.getSolverStatus(jobId);
    }

    @Operation(summary = "Get the score analysis for a schedule")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The score analysis",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ScoreAnalysis.class))),
            @APIResponse(responseCode = "404", description = "Schedule not found")})
    @GET
    @Path("{jobId}/score-analysis")
    @Produces(MediaType.APPLICATION_JSON)
    public ScoreAnalysis<HardSoftScore> getScoreAnalysis(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId,
            @QueryParam("fetchPolicy") ScoreAnalysisFetchPolicy fetchPolicy) {
        RollingStockSchedule schedule = getSchedule(jobId);
        if (fetchPolicy == null) {
            fetchPolicy = ScoreAnalysisFetchPolicy.FETCH_ALL;
        }
        return solutionManager.analyze(schedule, fetchPolicy);
    }

    @Operation(summary = "Submit a new schedule for solving")
    @APIResponses(value = {@APIResponse(responseCode = "200", description = "The job ID",
            content = @Content(mediaType = MediaType.TEXT_PLAIN))})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(RollingStockSchedule schedule) {
        String jobId = UUID.randomUUID().toString();
        scheduleMap.put(jobId, schedule);

        solverManager.solve(jobId, id -> scheduleMap.get(id),
                solution -> scheduleMap.put(jobId, solution));

        LOGGER.info("Started solving job: {}", jobId);
        return jobId;
    }

    @Operation(summary = "Update a schedule and continue solving")
    @APIResponses(value = {@APIResponse(responseCode = "200", description = "Updated successfully"),
            @APIResponse(responseCode = "404", description = "Schedule not found")})
    @PUT
    @Path("{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@Parameter(description = "The job ID") @PathParam("jobId") String jobId,
            RollingStockSchedule schedule) {
        if (!scheduleMap.containsKey(jobId)) {
            throw new ScheduleSolverException(jobId,
                    "Schedule with jobId '" + jobId + "' not found.");
        }
        scheduleMap.put(jobId, schedule);
        return Response.ok().build();
    }

    @Operation(summary = "Stop solving a schedule")
    @APIResponses(value = {@APIResponse(responseCode = "200", description = "Stopped successfully"),
            @APIResponse(responseCode = "404", description = "Schedule not found")})
    @DELETE
    @Path("{jobId}")
    public Response stopSolving(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        solverManager.terminateEarly(jobId);
        LOGGER.info("Terminated solving job: {}", jobId);
        return Response.ok().build();
    }

    @Operation(summary = "Delete a schedule")
    @APIResponses(
            value = {@APIResponse(responseCode = "200", description = "Deleted successfully")})
    @DELETE
    @Path("{jobId}/delete")
    public Response deleteSchedule(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        solverManager.terminateEarly(jobId);
        scheduleMap.remove(jobId);
        LOGGER.info("Deleted job: {}", jobId);
        return Response.ok().build();
    }
}
