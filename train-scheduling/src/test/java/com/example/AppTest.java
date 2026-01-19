package com.example;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.example.domain.RollingStockSchedule;
import com.example.rest.DemoDataGenerator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
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

    // Launches the solver on the supplied generator data set - so as to quickly iterate in the
    // terminal without the need of a web GUI
    @Test
    @Disabled("Only used manually for testing the solver in CLI")
    public void solveDemoData() throws InterruptedException, ExecutionException {
        RollingStockSchedule problem = demoDataGenerator.generateSmallDataset();
        DemoDataGenerator.printGeneratedData(problem);
        UUID problemId = UUID.randomUUID();

        RollingStockSchedule solution =
                solverManager.solve(problemId, problem).getFinalBestSolution();

        assertNotNull(solution);
        assertNotNull(solution.getScore());
        assertEquals(SolverStatus.NOT_SOLVING, solverManager.getSolverStatus(problemId));
    }
}
