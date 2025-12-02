package org.acme.rollingstockrostering.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stacija (Station) - Problem Fact
 * 
 * Represents a train station with its location and neighboring stations.
 * This is a problem fact used by the constraints to validate routes.
 */
public class Stacija {
    
    private Long id;
    private String nosaukums; // Station name
    private GeoCoordinates koordinatas; // Geographic coordinates
    private List<Long> kaiminiStacijas; // Neighbor station IDs
    
    public Stacija() {
        this.kaiminiStacijas = new ArrayList<>();
    }
    
    public Stacija(Long id, String nosaukums, GeoCoordinates koordinatas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.koordinatas = koordinatas;
        this.kaiminiStacijas = new ArrayList<>();
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
    
    public GeoCoordinates getKoordinatas() {
        return koordinatas;
    }
    
    public void setKoordinatas(GeoCoordinates koordinatas) {
        this.koordinatas = koordinatas;
    }
    
    public List<Long> getKaiminiStacijas() {
        return kaiminiStacijas;
    }
    
    public void setKaiminiStacijas(List<Long> kaiminiStacijas) {
        this.kaiminiStacijas = kaiminiStacijas;
    }
    
    @Override
    public String toString() {
        return "Stacija{" +
                "id=" + id +
                ", nosaukums='" + nosaukums + '\'' +
                ", koordinatas=" + koordinatas +
                '}';
    }
}
