package com.example.solver;

import com.example.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

/**
 * Tests for hard constraints
 */
class HardConstraintTest extends BaseConstraintTest {

        @Test
        void trainStartsAtDepo_whenStartsAtAssignedDepo_noPenalty() {
                // Train is at its depo
                TrainDepoAssignment assignment = new TrainDepoAssignment(1L, train1, depoA);
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainStartsAtDepo)
                                .given(departure, assignment).penalizesBy(0);
        }

        @Test
        void trainStartsAtDepo_whenStartsAtWrongStation_penalizes() {
                // Train is at station A, but its depo is at B
                TrainDepoAssignment assignment = new TrainDepoAssignment(1L, train1, depoB);
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainStartsAtDepo)
                                .given(departure, assignment).penalizesBy(1);
        }

        @Test
        void trainEndsAtDepo_whenEndsAtAssignedDepo_noPenalty() {
                // Train has a route that ends at its depo
                TrainDepoAssignment assignment = new TrainDepoAssignment(1L, train1, depoB);
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainEndsAtDepo)
                                .given(departure, assignment).penalizesBy(0);
        }

        @Test
        void trainEndsAtDepo_whenEndsAtWrongStation_penalizes() {
                // Train has a route that ends not at its depo
                TrainDepoAssignment assignment = new TrainDepoAssignment(1L, train1, depoA);
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainEndsAtDepo)
                                .given(departure, assignment).penalizesBy(1);
        }

        @Test
        void noOverlappingTrips_whenTripsDoNotOverlap_noPenalty() {
                // Two trips for the same train that don't overlap
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 = new RouteDeparture(2L, routeBA, train1,
                                LocalTime.of(12, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::noOverlappingTrips)
                                .given(departure1, departure2).penalizesBy(0);
        }

        @Test
        void noOverlappingTrips_whenTripsOverlap_penalizes() {
                // Two trips for the same train at the same time
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 =
                                new RouteDeparture(2L, routeBA, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::noOverlappingTrips)
                                .given(departure1, departure2).penalizesBy(1);
        }

        @Test
        void noOverlappingTrips_differentTrains_noPenalty() {
                // Two different trains can run at the same time
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 =
                                new RouteDeparture(2L, routeAB, train2, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::noOverlappingTrips)
                                .given(departure1, departure2).penalizesBy(0);
        }

        @Test
        void trainContinuity_whenTripsConnect_noPenalty() {
                // First trip ends at B, second trip starts at B
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 = new RouteDeparture(2L, routeBA, train1,
                                LocalTime.of(12, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainContinuity)
                                .given(departure1, departure2).penalizesBy(0);
        }

        @Test
        void trainContinuity_whenTripsDoNotConnect_penalizes() {
                // First trip A->B ends at B, second trip A->B starts at A (gap!)
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 = new RouteDeparture(2L, routeAB, train1,
                                LocalTime.of(12, 0), config);

                constraintVerifier.verifyThat(TrainScheduleConstraintProvider::trainContinuity)
                                .given(departure1, departure2).penalizesBy(1);
        }

        @Test
        void minIntervalBetweenDepartures_whenSufficientInterval_noPenalty() {
                // Two different trains departing from same station with 10 min gap (> 5 min
                // minimum)
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 = new RouteDeparture(2L, routeAB, train2,
                                LocalTime.of(8, 10), config);

                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::minIntervalBetweenDepartures)
                                .given(departure1, departure2, config).penalizesBy(0);
        }

        @Test
        void minIntervalBetweenDepartures_whenInsufficientInterval_penalizes() {
                // Two different trains departing from same station within 2 min - insufficient
                // delta
                RouteDeparture departure1 =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);
                RouteDeparture departure2 =
                                new RouteDeparture(2L, routeAB, train2, LocalTime.of(8, 2), config);

                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::minIntervalBetweenDepartures)
                                .given(departure1, departure2, config).penalizesBy(1);
        }

        @Test
        void depotCapacityNotExceeded_whenWithinCapacity_noPenalty() {
                // Depo has sufficient capacity
                TrainDepoAssignment assignment1 = new TrainDepoAssignment(1L, train1, depoA);
                TrainDepoAssignment assignment2 = new TrainDepoAssignment(2L, train2, depoA);

                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::depotCapacityNotExceeded)
                                .given(depoA, assignment1, assignment2).penalizesBy(0);
        }

        @Test
        void depotCapacityNotExceeded_whenOverCapacity_penalizes() {
                // Depo does not have sufficient capacity
                Depo smallDepo = new Depo(3L, stationB, 2);
                Train train3 = new Train(3L, 80);

                TrainDepoAssignment assignment1 = new TrainDepoAssignment(1L, train1, smallDepo);
                TrainDepoAssignment assignment2 = new TrainDepoAssignment(2L, train2, smallDepo);
                TrainDepoAssignment assignment3 = new TrainDepoAssignment(3L, train3, smallDepo);

                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::depotCapacityNotExceeded)
                                .given(smallDepo, assignment1, assignment2, assignment3)
                                .penalizesBy(1);
        }
}
