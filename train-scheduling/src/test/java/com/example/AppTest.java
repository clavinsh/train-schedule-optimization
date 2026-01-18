package com.example;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.example.domain.RollingStockSchedule;
import com.example.rest.DemoDataGenerator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class AppTest {

    @Inject
    SolverManager<RollingStockSchedule, UUID> solverManager;

    @Inject
    DemoDataGenerator demoDataGenerator;

    @Test
    public void solveDemoData() throws InterruptedException, ExecutionException {
        RollingStockSchedule problem = demoDataGenerator.generateSmallDataset();
        DemoDataGenerator.printGeneratedData(problem);
        // A specific problemId is recommended for a typical real-time planning schedule
        UUID problemId = UUID.randomUUID();

        // Solve the problem with the Timefold solver
        RollingStockSchedule solution = solverManager.solve(problemId, problem)
                .getFinalBestSolution();

        assertNotNull(solution);
        assertNotNull(solution.getScore());
        assertEquals(SolverStatus.NOT_SOLVING, solverManager.getSolverStatus(problemId));
    }
}