package org.acme.rollingstockrostering.domain;

import java.time.Duration;

/**
 * Konfigurācija (Configuration) - Problem Fact
 * 
 * Global configuration parameters for the scheduling problem:
 * - Minimum time between trains
 * - Station stop duration
 */
public class Konfiguracija {
    
    private Duration attalumsStarpVilcieniem; // Minimum time between trains
    private Duration stavesanasLaiks; // Station stop duration
    
    public Konfiguracija() {
    }
    
    public Konfiguracija(Duration attalumsStarpVilcieniem, Duration stavesanasLaiks) {
        this.attalumsStarpVilcieniem = attalumsStarpVilcieniem;
        this.stavesanasLaiks = stavesanasLaiks;
    }
    
    // Getters and setters
    public Duration getAttalumsStarpVilcieniem() {
        return attalumsStarpVilcieniem;
    }
    
    public void setAttalumsStarpVilcieniem(Duration attalumsStarpVilcieniem) {
        this.attalumsStarpVilcieniem = attalumsStarpVilcieniem;
    }
    
    public Duration getStavesanasLaiks() {
        return stavesanasLaiks;
    }
    
    public void setStavesanasLaiks(Duration stavesanasLaiks) {
        this.stavesanasLaiks = stavesanasLaiks;
    }
    
    @Override
    public String toString() {
        return "Konfiguracija{" +
                "attalumsStarpVilcieniem=" + attalumsStarpVilcieniem +
                ", stavesanasLaiks=" + stavesanasLaiks +
                '}';
    }
}
