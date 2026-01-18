package org.acme.rollingstockrostering.domain;

import java.time.Duration;

public class Konfiguracija {
    private Duration minimālaisIntervāls;  // Min time between trains at same station
    private Duration stāvēšanasLaiks;      // Stop duration at each station

    public Konfiguracija() {}

    public Konfiguracija(Duration minimālaisIntervāls, Duration stāvēšanasLaiks) {
        this.minimālaisIntervāls = minimālaisIntervāls;
        this.stāvēšanasLaiks = stāvēšanasLaiks;
    }

    // Getters and setters
    public Duration getMinimālaisIntervāls() { return minimālaisIntervāls; }
    public void setMinimālaisIntervāls(Duration minimālaisIntervāls) { this.minimālaisIntervāls = minimālaisIntervāls; }
    public Duration getStāvēšanasLaiks() { return stāvēšanasLaiks; }
    public void setStāvēšanasLaiks(Duration stāvēšanasLaiks) { this.stāvēšanasLaiks = stāvēšanasLaiks; }
}
