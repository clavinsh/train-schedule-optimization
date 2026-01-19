package com.example;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.example.domain.ScheduledTrip;
import com.example.domain.TrainSchedule;
import com.example.rest.DemoDataGenerator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AppTest {

    @Inject
    SolverManager<TrainSchedule, UUID> solverManager;

    @Inject
    DemoDataGenerator demoDataGenerator;

    @Test
    public void solveDemoData() throws InterruptedException, ExecutionException {
        // Generate problem
        TrainSchedule problem = DemoDataGenerator.generateSmallDataset();
        DemoDataGenerator.printGeneratedData(problem);

        UUID problemId = UUID.randomUUID();

        // Solve the problem
        System.out.println("\n=== STARTING SOLVER ===\n");
        TrainSchedule solution = solverManager.solve(problemId, problem).getFinalBestSolution();

        // Verify solution
        assertNotNull(solution, "Solution should not be null");
        assertNotNull(solution.getScore(), "Score should not be null");
        assertEquals(SolverStatus.NOT_SOLVING, solverManager.getSolverStatus(problemId));

        // Print solution summary
        printSolution(solution);

        // Assert feasibility (no hard constraint violations)
        assertTrue(solution.getScore().isFeasible(),
                "Solution should be feasible (0 hard violations), but was: " + solution.getScore());
    }

    private void printSolution(TrainSchedule solution) {
        System.out.println("\n=== SOLUTION SUMMARY ===\n");
        System.out.println("Final Score: " + solution.getScore());
        System.out.println("  - Hard: " + solution.getScore().hardScore());
        System.out.println("  - Medium: " + solution.getScore().mediumScore());
        System.out.println("  - Soft: " + solution.getScore().softScore());

        System.out.println("\n--- Scheduled Trips (sorted by train and time) ---\n");

        // Group trips by train and sort by departure time
        solution.getScheduledTrips().stream()
                .filter(trip -> trip.getAssignedTrain() != null && trip.getDepartureTime() != null)
                .sorted(Comparator
                        .comparing((ScheduledTrip t) -> t.getAssignedTrain().getId())
                        .thenComparing(ScheduledTrip::getDepartureTime))
                .forEach(trip -> {
                    System.out.printf("  Train %d | %s | %s %s | Departs: %s%n",
                            trip.getAssignedTrain().getId(),
                            trip.getRoute().getName(),
                            trip.getDirection(),
                            trip.getRoute().getFirstStop(trip.getDirection()).getName()
                                    + " -> " + trip.getRoute().getLastStop(trip.getDirection()).getName(),
                            trip.getDepartureTime().toLocalTime());
                });

        // Count unassigned trips
        long unassignedCount = solution.getScheduledTrips().stream()
                .filter(trip -> trip.getAssignedTrain() == null || trip.getDepartureTime() == null)
                .count();

        if (unassignedCount > 0) {
            System.out.println("\nWARNING: " + unassignedCount + " trips remain unassigned!");
        }

        System.out.println("\n=== END OF SOLUTION ===\n");
    }
}
