package org.acme.rollingstockrostering.domain;

import java.util.List;

public class Marsruts {
    private Long id;
    private String nosaukums;
    private List<Long> stacijas;  // Ordered list of station IDs

    public Marsruts() {}

    public Marsruts(Long id, String nosaukums, List<Long> stacijas) {
        this.id = id;
        this.nosaukums = nosaukums;
        this.stacijas = stacijas;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNosaukums() { return nosaukums; }
    public void setNosaukums(String nosaukums) { this.nosaukums = nosaukums; }
    public List<Long> getStacijas() { return stacijas; }
    public void setStacijas(List<Long> stacijas) { this.stacijas = stacijas; }
}
