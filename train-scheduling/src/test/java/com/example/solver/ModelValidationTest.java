package com.example.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.example.domain.RouteDeparture;
import com.example.domain.TrainDepoAssignment;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

/**
 * Tests for validating model correctness
 */
class ModelValidationTest extends BaseConstraintTest {

    @Test
    void verifyAllConstraintsDefinedProperly() {
        // This test verifies that all constraints are properly defined
        // and don't throw exceptions when the solver runs
        TrainDepoAssignment assignment = new TrainDepoAssignment(1L, train1, depoA);
        RouteDeparture departure =
                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

        // Verify the full constraint set produces a valid score
        // Train starts at A (depo at A) and ends at B -> ends at wrong depot = 1 hard penalty
        // Underutilized trip (0 passengers < 20% of 100) = 100 soft penalty
        constraintVerifier.verifyThat().given(departure, assignment, depoA, config)
                .scores(HardSoftScore.of(-1, -100));
    }

    @Test
    void verifyUnassignedVariablesHandled() {
        // Test that constraints handle unassigned planning variables gracefully
        RouteDeparture unassigned = new RouteDeparture(1L, routeAB, config);
        unassigned.setTrain(null);
        unassigned.setDepartureTime(null);

        // Should not throw - unassigned entities are filtered out by constraints
        constraintVerifier.verifyThat().given(unassigned, depoA, config).scores(HardSoftScore.ZERO);
    }
}
