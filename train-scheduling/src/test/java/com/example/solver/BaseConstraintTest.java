package com.example.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.example.domain.*;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.Arrays;

/**
 * Base class providing common test fixtures for constraint tests.
 */
abstract class BaseConstraintTest {

    protected ConstraintVerifier<TrainScheduleConstraintProvider, RollingStockSchedule> constraintVerifier;

    // Reusable test fixtures
    protected Station stationA;
    protected Station stationB;
    protected Station stationC;
    protected Train train1;
    protected Train train2;
    protected Route routeAB;
    protected Route routeBA;
    protected Route routeABC;
    protected Depo depoA;
    protected Depo depoB;
    protected TrainConfiguration config;

    @BeforeEach
    void setUp() {
        constraintVerifier = ConstraintVerifier.build(new TrainScheduleConstraintProvider(),
                RollingStockSchedule.class, RouteDeparture.class);

        stationA = new Station(1L, "Station A", 56.95, 24.10, null);
        stationB = new Station(2L, "Station B", 56.96, 24.11, null);
        stationC = new Station(3L, "Station C", 56.97, 24.12, null);

        train1 = new Train(1L, 100);
        train2 = new Train(2L, 50);

        routeAB = new Route(1L, "A-B", Arrays.asList(stationA, stationB));
        routeBA = new Route(2L, "B-A", Arrays.asList(stationB, stationA));
        routeABC = new Route(3L, "A-B-C", Arrays.asList(stationA, stationB, stationC));

        depoA = new Depo(1L, stationA, 5);
        depoB = new Depo(2L, stationB, 3);

        config = new TrainConfiguration(Duration.ofMinutes(5), Duration.ofMinutes(2), 60.0);
    }
}
