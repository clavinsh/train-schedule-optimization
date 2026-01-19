package com.example.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import com.example.domain.RollingStockSchedule;
import com.example.persistence.RollingStockScheduleSolutionFileIO;
import com.example.rest.DemoDataGenerator;

import java.io.File;

/**
 * Standalone benchmark runner for train scheduling optimization.
 * Run from terminal: mvn exec:java -Dexec.mainClass="com.example.benchmark.BenchmarkRunner"
 * Or with arguments: mvn exec:java -Dexec.mainClass="com.example.benchmark.BenchmarkRunner" -Dexec.args="small"
 */
public class BenchmarkRunner {

    public static void main(String[] args) {
        String datasetSize = args.length > 0 ? args[0].toLowerCase() : "default";
        
        System.out.println("===========================================");
        System.out.println("  Timefold Train Scheduling Benchmark");
        System.out.println("===========================================");
        System.out.println("Dataset size: " + datasetSize);
        System.out.println();

        // Generate dataset based on size argument
        RollingStockSchedule problem = switch (datasetSize) {
            case "small" -> DemoDataGenerator.generateSmallDataset();
            case "large" -> DemoDataGenerator.generateLargeDataset();
            default -> DemoDataGenerator.generateDefaultDataset();
        };

        System.out.println("Problem statistics:");
        System.out.println("  - Trains: " + problem.getTrains().size());
        System.out.println("  - Routes: " + problem.getRoutes().size());
        System.out.println("  - Stations: " + problem.getStations().size());
        System.out.println("  - Departure times: " + problem.getDepartureTimes().size());
        System.out.println("  - Depos: " + problem.getDepos().size());
        System.out.println();

        // Create benchmark data directory and save input problem
        File dataDir = new File("local/benchmarkData");
        dataDir.mkdirs();
        
        RollingStockScheduleSolutionFileIO fileIO = new RollingStockScheduleSolutionFileIO();
        File inputFile = new File(dataDir, "trainSchedule-" + datasetSize + ".json");
        fileIO.write(problem, inputFile);
        System.out.println("Saved input problem to: " + inputFile.getAbsolutePath());
        System.out.println();

        // Run benchmark using XML configuration
        System.out.println("Starting benchmark... This may take several minutes.");
        System.out.println("Each algorithm will run for 30 seconds after 10 second warmup.");
        System.out.println();

        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory
                .createFromXmlResource("benchmarkConfig.xml");
        
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem);
        benchmark.benchmarkAndShowReportInBrowser();

        System.out.println();
        System.out.println("===========================================");
        System.out.println("  Benchmark complete!");
        System.out.println("  Report opened in browser.");
        System.out.println("  Report location: local/benchmarkReport/");
        System.out.println("===========================================");
    }
}
