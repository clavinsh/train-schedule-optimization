package org.acme.rollingstockrostering.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import org.acme.rollingstockrostering.domain.RollingStockSchedule;
import org.acme.rollingstockrostering.rest.DemoDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark runner application - palaiž Timefold Solver benchmarking ar dažādiem algoritmiem.
 * 
 * Palaišana no komandas līnijas:
 * ./mvnw.cmd exec:java -Dexec.mainClass="org.acme.rollingstockrostering.benchmark.RollingStockBenchmarkApp"
 * 
 * Rezultāti tiks saglabāti: target/benchmark/
 */
public class RollingStockBenchmarkApp {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Rolling Stock Rostering Optimization - Benchmark");
        System.out.println("=".repeat(80));
        
        PlannerBenchmarkFactory benchmarkFactory = 
            PlannerBenchmarkFactory.createFromXmlResource("benchmarkConfig.xml");
        
        // Ģenerē test datus dažādos izmēros
        List<RollingStockSchedule> problemList = new ArrayList<>();
        
        // Mazs datasets (50 atiešanas)
        System.out.println("Ģenerē mazu dataset (50 atiešanas)...");
        problemList.add(DemoDataGenerator.generateSmallDataset());
        
        // Vidējs datasets (99 atiešanas - default)
        System.out.println("Ģenerē vidēju dataset (99 atiešanas)...");
        problemList.add(DemoDataGenerator.generateDefaultDataset());
        
        // Liels datasets (200 atiešanas)
        System.out.println("Ģenerē lielu dataset (200 atiešanas)...");
        problemList.add(DemoDataGenerator.generateLargeDataset());
        
        System.out.println("\nSākas benchmarking ar " + problemList.size() + " problēmām...");
        System.out.println("Katrs solvers tiks testēts 30 sekundes uz katru problēmu.");
        System.out.println("Kopējais laiks: ~" + (problemList.size() * 6 * 0.5 + 0.5) + " minūtes");
        System.out.println("=".repeat(80));
        
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(
            problemList.toArray(new RollingStockSchedule[0])
        );
        
        benchmark.benchmarkAndShowReportInBrowser();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Benchmark pabeigts! Rezultāti: target/benchmark/index.html");
        System.out.println("=".repeat(80));
    }
}
