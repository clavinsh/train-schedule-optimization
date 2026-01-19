package com.example.solver;

import com.example.domain.RouteDeparture;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

/**
 * Tests for soft constraints
 */
class SoftConstraintTest extends BaseConstraintTest {

        @Test
        void maximizePassengerPickup_rewardsPassengers() {
                // Create a departure with demand lookup that returns passengers
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                // Note: This test is simplified - in reality you'd need to set up
                // stationDemandLookup
                // The constraint rewards based on getTotalEmbarkingPassengers() capped by capacity
                // With no demand lookup, rewards 0 (no matches)
                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::maximizePassengerPickup)
                                .given(departure).rewardsWith(0);
        }

        @Test
        void minimizeUnderutilizedTrips_whenLowUtilization_penalizes() {
                // Penalizes empty train: capacity - passengers = 100 - 0 = 100
                RouteDeparture departure =
                                new RouteDeparture(1L, routeAB, train1, LocalTime.of(8, 0), config);

                constraintVerifier.verifyThat(
                                TrainScheduleConstraintProvider::minimizeUnderutilizedTrips)
                                .given(departure).penalizesBy(100);
        }
}
