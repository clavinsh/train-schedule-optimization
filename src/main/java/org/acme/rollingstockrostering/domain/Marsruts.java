package org.acme.rollingstockrostering.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Maršruts (Route) - Problem Fact
 * 
 * Represents a train route with an ordered list of stations.
 * This is used to validate that assigned trains visit all required stations.
 */
public class Marsruts {
    
    private Long id;
    private String nosaukums; // Route name
    private List<Long> stacijas; // Ordered list of station IDs
    
    public Marsruts() {
        this.stacijas = new ArrayList<>();
    }
    
    public Marsruts(Long id, String nosaukums, List<Long> stacijas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.stacijas = stacijas;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNosaukums() {
        return nosaukums;
    }
    
    public void setNosaukums(String nosaukums) {
        this.nosaukums = nosaukums;
    }
    
    public List<Long> getStacijas() {
        return stacijas;
    }
    
    public void setStacijas(List<Long> stacijas) {
        this.stacijas = stacijas;
    }
    
    @Override
    public String toString() {
        return "Marsruts{" +
                "id=" + id +
                ", nosaukums='" + nosaukums + '\'' +
                ", stacijas=" + stacijas +
                '}';
    }
}
