package org.acme.rollingstockrostering.domain;

import java.util.List;

public class Stacija {
    private Long id;
    private String nosaukums;
    private GeoCoordinates koordinatas;
    private List<Long> kaiminiStacijas;

    public Stacija() {}

    public Stacija(Long id, String nosaukums, GeoCoordinates koordinatas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.koordinatas = koordinatas;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNosaukums() { return nosaukums; }
    public void setNosaukums(String nosaukums) { this.nosaukums = nosaukums; }
    public GeoCoordinates getKoordinatas() { return koordinatas; }
    public void setKoordinatas(GeoCoordinates koordinatas) { this.koordinatas = koordinatas; }
    public List<Long> getKaiminiStacijas() { return kaiminiStacijas; }
    public void setKaiminiStacijas(List<Long> kaiminiStacijas) { this.kaiminiStacijas = kaiminiStacijas; }
}
