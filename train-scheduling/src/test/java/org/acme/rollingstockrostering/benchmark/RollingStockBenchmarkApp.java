package org.acme.rollingstockrostering.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import org.acme.rollingstockrostering.domain.RollingStockSchedule;
import org.acme.rollingstockrostering.rest.DemoDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark runner application - runs Timefold Solver benchmarking with different algorithms.
 *
 * Run from command line:
 * ./mvnw exec:java -Dexec.mainClass="org.acme.rollingstockrostering.benchmark.RollingStockBenchmarkApp"
 *
 * Results will be saved to: target/benchmark/
 */
public class RollingStockBenchmarkApp {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Rolling Stock Rostering Optimization - Benchmark");
        System.out.println("=".repeat(80));

        PlannerBenchmarkFactory benchmarkFactory =
            PlannerBenchmarkFactory.createFromXmlResource("benchmarkConfig.xml");

        // Generate test datasets of different sizes
        List<RollingStockSchedule> problemList = new ArrayList<>();

        // Small dataset (~40 trips)
        System.out.println("Generating small dataset (~40 trips)...");
        RollingStockSchedule small = DemoDataGenerator.generateSmallDataset();
        System.out.println("  - " + small.getTrips().size() + " trips");
        System.out.println("  - " + small.getVilcieni().size() + " trains");
        System.out.println("  - " + small.getMarsruti().size() + " routes");
        problemList.add(small);

        // Default dataset (~80 trips)
        System.out.println("Generating default dataset (~80 trips)...");
        RollingStockSchedule medium = DemoDataGenerator.generateDefaultDataset();
        System.out.println("  - " + medium.getTrips().size() + " trips");
        System.out.println("  - " + medium.getVilcieni().size() + " trains");
        System.out.println("  - " + medium.getMarsruti().size() + " routes");
        problemList.add(medium);

        // Large dataset (~160 trips)
        System.out.println("Generating large dataset (~160 trips)...");
        RollingStockSchedule large = DemoDataGenerator.generateLargeDataset();
        System.out.println("  - " + large.getTrips().size() + " trips");
        System.out.println("  - " + large.getVilcieni().size() + " trains");
        System.out.println("  - " + large.getMarsruti().size() + " routes");
        problemList.add(large);

        System.out.println("\nStarting benchmark with " + problemList.size() + " problem sizes...");
        System.out.println("Each solver will be tested for 30 seconds on each problem.");
        System.out.println("Total estimated time: ~" + (problemList.size() * 6 * 0.5 + 0.5) + " minutes");
        System.out.println("=".repeat(80));

        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(
            problemList.toArray(new RollingStockSchedule[0])
        );

        benchmark.benchmarkAndShowReportInBrowser();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Benchmark complete! Results: target/benchmark/index.html");
        System.out.println("=".repeat(80));
    }
}
