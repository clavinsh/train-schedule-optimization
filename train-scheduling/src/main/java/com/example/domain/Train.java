package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Train {
    private int id;
    private Station depot;
    private int passengerCapacity;
    private double speedFactor; // 1.0 = normal, 1.2 = 20% faster

    public int getAdjustedTravelTime(int baseTravelTime) {
        if (speedFactor <= 0) {
            return baseTravelTime;
        }
        return (int) (baseTravelTime / speedFactor);
    }

    public boolean canServeRoute(Route route) {
        // For now, all trains can serve all routes
        // Can be extended later for capability checks (e.g., train type, track gauge)
        return true;
    }
}
