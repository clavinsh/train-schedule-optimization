package com.example.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.core.config.solver.SolverConfig;
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
import com.example.solver.SolverConfigFactory;

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

    @Inject
    SolverConfigFactory solverConfigFactory;

    // In-memory storage for schedules (in production, use a database)
    private final ConcurrentMap<String, RollingStockSchedule> scheduleMap =
            new ConcurrentHashMap<>();
    
    // Track algorithm used per job for reporting
    private final ConcurrentMap<String, String> jobAlgorithmMap =
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

    @Operation(summary = "Get passenger statistics for a schedule")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Passenger statistics",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404", description = "Schedule not found")})
    @GET
    @Path("{jobId}/passenger-stats")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.Map<String, Object> getPassengerStatistics(
            @Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        RollingStockSchedule schedule = getSchedule(jobId);

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        java.util.List<java.util.Map<String, Object>> tripDetails = new java.util.ArrayList<>();

        int totalTrips = 0;
        int assignedTrips = 0;
        int tripsWithPassengers = 0;
        int tripsOverCapacity = 0;
        int totalPassengers = 0;
        int totalMaxLoad = 0;

        for (var departure : schedule.getRouteDepartures()) {
            totalTrips++;
            if (departure.getTrain() != null && departure.getDepartureTime() != null) {
                assignedTrips++;
                int embarking = departure.getTotalEmbarkingPassengers();
                int disembarking = departure.getTotalDisembarkingPassengers();
                int maxLoad = departure.getMaxPassengerLoad();
                boolean overCapacity = departure.exceedsCapacity();

                if (embarking > 0) {
                    tripsWithPassengers++;
                    totalPassengers += embarking;
                }

                totalMaxLoad += maxLoad;

                if (overCapacity) {
                    tripsOverCapacity++;
                }

                // Add detailed trip info
                java.util.Map<String, Object> tripInfo = new java.util.LinkedHashMap<>();
                tripInfo.put("tripId", departure.getId());
                tripInfo.put("route", departure.getRoute() != null ? departure.getRoute().getName() : null);
                tripInfo.put("trainId", departure.getTrain().getId());
                tripInfo.put("trainCapacity", departure.getTrain().getCapacity());
                tripInfo.put("departureTime", departure.getDepartureTime() != null ? departure.getDepartureTime().toString() : null);
                tripInfo.put("totalEmbarkingPassengers", embarking);
                tripInfo.put("totalDisembarkingPassengers", disembarking);
                tripInfo.put("maxPassengerLoad", maxLoad);
                tripInfo.put("exceedsCapacity", overCapacity);
                tripInfo.put("capacityOverflow", departure.getCapacityOverflow());

                tripDetails.add(tripInfo);
            }
        }

        stats.put("totalTrips", totalTrips);
        stats.put("assignedTrips", assignedTrips);
        stats.put("tripsWithPassengers", tripsWithPassengers);
        stats.put("totalPassengersServed", totalPassengers);
        stats.put("averageLoadPerTrip", assignedTrips > 0 ? totalMaxLoad / assignedTrips : 0);
        stats.put("tripsOverCapacity", tripsOverCapacity);
        stats.put("trips", tripDetails);

        return stats;
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
    public String solve(RollingStockSchedule schedule,
            @Parameter(description = "Algorithm to use: default, tabu, late-acceptance, simulated-annealing")
            @QueryParam("algorithm") String algorithm) {
        String jobId = UUID.randomUUID().toString();

        // Rebuild demand lookup for each RouteDeparture (lost during JSON deserialization)
        rebuildDemandLookups(schedule);

        scheduleMap.put(jobId, schedule);
        jobAlgorithmMap.put(jobId, algorithm != null ? algorithm : "default");

        DemoDataGenerator.printGeneratedData(schedule);

        // Use default solver manager for standard algorithm, or custom config for others
        if (algorithm == null || "default".equalsIgnoreCase(algorithm) || "late-acceptance".equalsIgnoreCase(algorithm)) {
            // Default solver from solverConfig.xml uses Late Acceptance
            solverManager.solve(jobId, id -> scheduleMap.get(id),
                    solution -> scheduleMap.put(jobId, solution));
        } else {
            // Create custom solver config for other algorithms
            SolverConfig solverConfig = solverConfigFactory.createSolverConfig(algorithm, false);
            SolverFactory<RollingStockSchedule> solverFactory = SolverFactory.create(solverConfig);
            
            // Run solver in background thread
            new Thread(() -> {
                try {
                    RollingStockSchedule solution = solverFactory.buildSolver().solve(schedule);
                    scheduleMap.put(jobId, solution);
                    LOGGER.info("Completed solving job {} with algorithm {}", jobId, algorithm);
                } catch (Exception e) {
                    LOGGER.error("Error solving job {} with algorithm {}: {}", jobId, algorithm, e.getMessage());
                }
            }, "solver-" + jobId).start();
        }

        LOGGER.info("Started solving job: {} with algorithm: {}", jobId, 
                solverConfigFactory.getAlgorithmDisplayName(algorithm));
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

    @Operation(summary = "Run benchmark comparing all algorithms")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Benchmark results",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON))})
    @POST
    @Path("benchmark")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runBenchmark(RollingStockSchedule schedule,
            @Parameter(description = "Time limit in seconds for each algorithm")
            @QueryParam("timeLimit") Integer timeLimitSeconds) {

        // Rebuild demand lookup (lost during JSON deserialization)
        rebuildDemandLookups(schedule);

        LOGGER.info("Starting benchmark with {} trains, {} routes, {} departures",
                schedule.getTrains().size(), schedule.getRoutes().size(), schedule.getRouteDepartures().size());

        String[] algorithms = {"default", "tabu", "late-acceptance", "simulated-annealing"};
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();

        for (String algorithm : algorithms) {
            LOGGER.info("Benchmarking algorithm: {}", algorithm);
            
            // Clone the schedule for each algorithm
            RollingStockSchedule clonedSchedule = cloneSchedule(schedule);
            
            long startTime = System.currentTimeMillis();
            
            // Create solver config with benchmark termination
            SolverConfig solverConfig = solverConfigFactory.createSolverConfig(algorithm, true);
            if (timeLimitSeconds != null && timeLimitSeconds > 0) {
                solverConfig.getTerminationConfig().setSecondsSpentLimit((long) timeLimitSeconds);
            }
            
            SolverFactory<RollingStockSchedule> solverFactory = SolverFactory.create(solverConfig);
            RollingStockSchedule solution = solverFactory.buildSolver().solve(clonedSchedule);
            
            long endTime = System.currentTimeMillis();
            double durationSeconds = (endTime - startTime) / 1000.0;

            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("algorithm", algorithm);
            result.put("algorithmName", solverConfigFactory.getAlgorithmDisplayName(algorithm));
            result.put("hardScore", solution.getScore() != null ? solution.getScore().hardScore() : 0);
            result.put("softScore", solution.getScore() != null ? solution.getScore().softScore() : 0);
            result.put("score", solution.getScore() != null ? solution.getScore().toString() : "N/A");
            result.put("timeSeconds", Math.round(durationSeconds * 100.0) / 100.0);
            
            results.add(result);
            LOGGER.info("Algorithm {} finished with score {} in {} seconds", 
                    algorithm, solution.getScore(), durationSeconds);
        }

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("problemSize", java.util.Map.of(
                "trains", schedule.getTrains().size(),
                "routes", schedule.getRoutes().size(),
                "departures", schedule.getRouteDepartures().size(),
                "stations", schedule.getStations().size()
        ));
        response.put("results", results);
        response.put("timestamp", java.time.Instant.now().toString());

        return Response.ok(response).build();
    }

    /**
     * Rebuilds the demand lookup for each RouteDeparture in the schedule.
     * This is necessary because the lookup is not serialized in JSON (@JsonIgnore),
     * so it's lost when the schedule is sent from the frontend.
     */
    private void rebuildDemandLookups(RollingStockSchedule schedule) {
        if (schedule.getRouteDepartures() == null || schedule.getStationDemands() == null) {
            return;
        }

        for (var routeDeparture : schedule.getRouteDepartures()) {
            if (routeDeparture.getRoute() == null) continue;

            // Build demand lookup for this route: Station -> (Hour -> StationDemand)
            java.util.Map<com.example.domain.Station, java.util.Map<Integer, com.example.domain.StationDemand>> lookup =
                    new java.util.HashMap<>();

            for (var demand : schedule.getStationDemands()) {
                // Only include demand for this specific route
                if (demand.getRoute() != null &&
                    demand.getRoute().getId().equals(routeDeparture.getRoute().getId())) {
                    lookup.computeIfAbsent(demand.getStation(), k -> new java.util.HashMap<>())
                            .put(demand.getTime().getHour(), demand);
                }
            }

            routeDeparture.setStationDemandLookup(lookup);
        }

        LOGGER.debug("Rebuilt demand lookups for {} route departures", schedule.getRouteDepartures().size());
    }

    /**
     * Creates a deep clone of the schedule for benchmark isolation.
     */
    private RollingStockSchedule cloneSchedule(RollingStockSchedule original) {
        RollingStockSchedule clone = new RollingStockSchedule();
        clone.setTrains(original.getTrains());
        clone.setRoutes(original.getRoutes());
        clone.setStations(original.getStations());
        clone.setTrainDepoAssignments(original.getTrainDepoAssignments());
        clone.setDepos(original.getDepos());
        clone.setStationDemands(original.getStationDemands());
        
        // Deep clone route departures to isolate planning variables
        java.util.List<com.example.domain.RouteDeparture> clonedDepartures = new java.util.ArrayList<>();
        for (com.example.domain.RouteDeparture rd : original.getRouteDepartures()) {
            com.example.domain.RouteDeparture clonedRd = new com.example.domain.RouteDeparture(
                    rd.getId(), rd.getRoute());
            clonedRd.setStationDemandLookup(rd.getStationDemandLookup());
            // Reset planning variables
            clonedRd.setTrain(null);
            clonedRd.setDepartureTime(null);
            clonedDepartures.add(clonedRd);
        }
        clone.setRouteDepartures(clonedDepartures);
        
        return clone;
    }
}
