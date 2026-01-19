package com.example.solver;

import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import ai.timefold.solver.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;

import com.example.domain.RouteDeparture;
import com.example.domain.RollingStockSchedule;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory for creating different solver configurations based on the selected algorithm.
 * Supports: Hill Climbing (default), Tabu Search, Late Acceptance, and Simulated Annealing.
 */
@ApplicationScoped
public class SolverConfigFactory {

    private static final long DEFAULT_TERMINATION_SECONDS = 120L;
    private static final long BENCHMARK_TERMINATION_SECONDS = 30L;

    /**
     * Creates a SolverConfig for the specified algorithm.
     *
     * @param algorithm The algorithm name (default, tabu, late-acceptance, simulated-annealing)
     * @param forBenchmark If true, uses shorter termination time for benchmarking
     * @return Configured SolverConfig
     */
    public SolverConfig createSolverConfig(String algorithm, boolean forBenchmark) {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(RollingStockSchedule.class)
                .withEntityClasses(RouteDeparture.class)
                .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                        .withConstraintProviderClass(TrainScheduleConstraintProvider.class));

        long terminationSeconds = forBenchmark ? BENCHMARK_TERMINATION_SECONDS : DEFAULT_TERMINATION_SECONDS;
        
        // Termination config
        TerminationConfig terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(terminationSeconds)
                .withUnimprovedSecondsSpentLimit(forBenchmark ? 15L : 60L);
        solverConfig.setTerminationConfig(terminationConfig);

        // Construction Heuristic phase - same for all algorithms
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig()
                .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING);

        // Local Search phase - varies by algorithm
        LocalSearchPhaseConfig localSearchPhaseConfig = createLocalSearchConfig(algorithm);

        solverConfig.withPhases(constructionHeuristicPhaseConfig, localSearchPhaseConfig);

        return solverConfig;
    }

    private LocalSearchPhaseConfig createLocalSearchConfig(String algorithm) {
        LocalSearchPhaseConfig config = new LocalSearchPhaseConfig();

        // Common move selector for all algorithms
        UnionMoveSelectorConfig moveSelectorConfig = new UnionMoveSelectorConfig()
                .withMoveSelectors(
                        new ChangeMoveSelectorConfig(),
                        new SwapMoveSelectorConfig()
                );
        config.setMoveSelectorConfig(moveSelectorConfig);

        // Algorithm-specific acceptor
        LocalSearchAcceptorConfig acceptorConfig = new LocalSearchAcceptorConfig();
        LocalSearchForagerConfig foragerConfig = new LocalSearchForagerConfig();

        switch (algorithm != null ? algorithm.toLowerCase() : "default") {
            case "tabu":
                // Tabu Search: remembers recent moves and forbids reversing them
                acceptorConfig.withEntityTabuSize(7);
                foragerConfig.withAcceptedCountLimit(1000);
                break;

            case "late-acceptance":
                // Late Acceptance: accepts moves that are better than the score from N steps ago
                acceptorConfig.withLateAcceptanceSize(400);
                foragerConfig.withAcceptedCountLimit(4);
                break;

            case "simulated-annealing":
                // Simulated Annealing: accepts worse moves with decreasing probability
                acceptorConfig.withSimulatedAnnealingStartingTemperature("0hard/500soft");
                foragerConfig.withAcceptedCountLimit(4);
                break;

            case "default":
            default:
                // Hill Climbing (greedy): only accepts improving moves
                acceptorConfig.withLateAcceptanceSize(1); // Effectively hill climbing
                foragerConfig.withAcceptedCountLimit(4);
                break;
        }

        config.setAcceptorConfig(acceptorConfig);
        config.setForagerConfig(foragerConfig);

        return config;
    }

    /**
     * Returns a human-readable name for the algorithm.
     */
    public String getAlgorithmDisplayName(String algorithm) {
        switch (algorithm != null ? algorithm.toLowerCase() : "default") {
            case "tabu":
                return "Tabu Search";
            case "late-acceptance":
                return "Late Acceptance";
            case "simulated-annealing":
                return "Simulated Annealing";
            case "default":
            default:
                return "Hill Climbing";
        }
    }
}
