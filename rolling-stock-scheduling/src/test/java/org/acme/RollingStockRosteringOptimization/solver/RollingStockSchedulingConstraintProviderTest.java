package org.acme.RollingStockRosteringOptimization.solver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.RollingStockRosteringOptimization.domain.*;
import org.acme.RollingStockRosteringOptimization.rest.DemoDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RollingStockSchedulingConstraintProviderTest {

    private ConstraintVerifier<RollingStockSchedulingConstraintProvider, RollingStockSchedule> constraintVerifier;
    private SolverFactory<RollingStockSchedule> solverFactory;
    private SolutionManager<RollingStockSchedule, HardSoftLongScore> solutionManager;
    private DemoDataGenerator demoDataGenerator;

    @BeforeEach
    void setUp() {
        // Create constraint verifier
        constraintVerifier = ConstraintVerifier.build(
                new RollingStockSchedulingConstraintProvider(),
                RollingStockSchedule.class,
                Ride.class);

        // Create solver factory from XML configuration
        solverFactory = SolverFactory.createFromXmlResource("rollingStockSolverConfig.xml");
        solutionManager = SolutionManager.create(solverFactory);
        demoDataGenerator = new DemoDataGenerator();
    }

    // ========================================================================
    // Test: Ride Conflict
    // ========================================================================
    @Test
    void rideConflict() {
        Train train = new Train("1", 100);
        Route route = new Route("1", "Test Route");

        Station station1 = new Station("1", "Station A");
        Station station2 = new Station("2", "Station B");

        LocalDateTime now = LocalDateTime.now();

        // Two overlapping rides assigned to the same train
        Ride ride1 = new Ride("1", route, station1, station2, now, now.plusMinutes(30));
        ride1.setTrain(train);

        Ride ride2 = new Ride("2", route, station1, station2, now.plusMinutes(15), now.plusMinutes(45));
        ride2.setTrain(train);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::rideConflict)
                .given(ride1, ride2)
                .penalizes(); // Overlapping rides for same train - should trigger penalty
    }

    @Test
    void rideConflict_noOverlap() {
        Train train = new Train("1", 100);
        Route route = new Route("1", "Test Route");

        Station station1 = new Station("1", "Station A");
        Station station2 = new Station("2", "Station B");

        LocalDateTime now = LocalDateTime.now();

        // Two non-overlapping rides
        Ride ride1 = new Ride("1", route, station1, station2, now, now.plusMinutes(30));
        ride1.setTrain(train);

        Ride ride2 = new Ride("2", route, station1, station2, now.plusMinutes(31), now.plusMinutes(60));
        ride2.setTrain(train);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::rideConflict)
                .given(ride1, ride2)
                .penalizesBy(0); // No overlap - no penalty
    }

    // ========================================================================
    // Test: Capacity Exceeded
    // ========================================================================
    @Test
    void capacityExceeded() {
        Train train = new Train("1", 50); // Small capacity
        Route route = new Route("1", "Test Route");

        Station station1 = new Station("1", "Rīga");
        Station station2 = new Station("2", "Station B");

        LocalDateTime now = LocalDateTime.now().withHour(8).withMinute(0);

        Ride ride = new Ride("1", route, station1, station2, now, now.plusMinutes(30));
        ride.setTrain(train);

        // Create demand that exceeds train capacity
        Map<Integer, Integer> hourlyDemand = new HashMap<>();
        hourlyDemand.put(8, 100); // 100 passengers at 8 AM, but train only holds 50
        Demand demand = new Demand("1", station1, hourlyDemand);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::capacityExceeded)
                .given(ride, demand)
                .penalizesBy(50L); // Exceeds by 50 passengers
    }

    @Test
    void capacityNotExceeded() {
        Train train = new Train("1", 200); // Large capacity
        Route route = new Route("1", "Test Route");

        Station station1 = new Station("1", "Rīga");
        Station station2 = new Station("2", "Station B");

        LocalDateTime now = LocalDateTime.now().withHour(8).withMinute(0);

        Ride ride = new Ride("1", route, station1, station2, now, now.plusMinutes(30));
        ride.setTrain(train);

        // Create demand within train capacity
        Map<Integer, Integer> hourlyDemand = new HashMap<>();
        hourlyDemand.put(8, 100); // 100 passengers at 8 AM, train holds 200
        Demand demand = new Demand("1", station1, hourlyDemand);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::capacityExceeded)
                .given(ride, demand)
                .penalizesBy(0L); // Within capacity
    }

    // ========================================================================
    // Test: First/Last Ride Depot Constraints
    // ========================================================================
    @Test
    void firstRideNotFromDepo() {
        Station depotStation = new Station("1", "Rīga");
        Station otherStation = new Station("2", "Other");
        Depo depo = new Depo("1", depotStation);

        Train train = new Train("1", 100);
        Route route = new Route("1", "Test Route");

        LocalDateTime now = LocalDateTime.now();

        // First ride starts from non-depot station
        Ride ride = new Ride("1", route, otherStation, depotStation, now, now.plusMinutes(30));
        ride.setTrain(train);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::firstRideNotFromDepo)
                .given(train, ride, depo)
                .penalizesBy(1L); // Not from depot
    }

    @Test
    void firstRideFromDepo() {
        Station depotStation = new Station("1", "Rīga");
        Station otherStation = new Station("2", "Other");
        Depo depo = new Depo("1", depotStation);

        Train train = new Train("1", 100);
        Route route = new Route("1", "Test Route");

        LocalDateTime now = LocalDateTime.now();

        // First ride starts from depot station
        Ride ride = new Ride("1", route, depotStation, otherStation, now, now.plusMinutes(30));
        ride.setTrain(train);

        constraintVerifier.verifyThat(RollingStockSchedulingConstraintProvider::firstRideNotFromDepo)
                .given(train, ride, depo)
                .penalizesBy(0L); // From depot - OK
    }

    // ========================================================================
    // Test: Run Full Solver with Demo Data
    // ========================================================================
    @Test
    void runSolverWithDemoData() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING SOLVER TEST WITH DEMO DATA");
        System.out.println("=".repeat(80));

        // Generate demo data
        RollingStockSchedule problem = demoDataGenerator.generateDemoData();
        demoDataGenerator.printScheduleSummary(problem);

        // Analyze initial score (before solving)
        System.out.println("\n--- INITIAL SCORE ANALYSIS (before solving) ---");
        var initialAnalysis = solutionManager.analyze(problem);
        System.out.println("Initial Score: " + initialAnalysis.score());

        System.out.println("\nConstraint breakdown:");
        initialAnalysis.constraintMap().forEach((constraintRef, constraintAnalysis) -> {
            System.out.println("  " + constraintRef.constraintName() + ": " + constraintAnalysis.score());
        });

        // Solve (with short timeout for testing)
        System.out.println("\n--- SOLVING ---");
        var solver = solverFactory.buildSolver();
        RollingStockSchedule solution = solver.solve(problem);

        // Output results
        System.out.println("\n--- SOLUTION SCORE ---");
        System.out.println("Final Score: " + solution.getScore());

        System.out.println("\n--- FINAL SCORE ANALYSIS ---");
        var finalAnalysis = solutionManager.analyze(solution);
        finalAnalysis.constraintMap().forEach((constraintRef, constraintAnalysis) -> {
            System.out.println("  " + constraintRef.constraintName() + ": " + constraintAnalysis.score());
        });

        // Count assigned rides
        long assignedRides = solution.getRides().stream()
                .filter(r -> r.getTrain() != null)
                .count();
        System.out.println("\n--- ASSIGNMENT STATISTICS ---");
        System.out.println("Total rides: " + solution.getRides().size());
        System.out.println("Assigned rides: " + assignedRides);
        System.out.println("Unassigned rides: " + (solution.getRides().size() - assignedRides));

        // Show sample assignments
        System.out.println("\n--- SAMPLE ASSIGNMENTS (first 10) ---");
        solution.getRides().stream()
                .filter(r -> r.getTrain() != null)
                .limit(10)
                .forEach(r -> System.out.println("  Ride " + r.getId() + " (" + r.getDepartureStation().getName()
                        + " -> " + r.getArrivalStation().getName() + ") assigned to Train-" + r.getTrain().getId()));

        System.out.println("\n" + "=".repeat(80) + "\n");

        // Assert that we got a valid score (not null)
        assert solution.getScore() != null : "Score should not be null";
    }
}
