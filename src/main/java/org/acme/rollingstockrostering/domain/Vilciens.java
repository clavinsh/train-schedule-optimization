package org.acme.rollingstockrostering.domain;

/**
 * Vilciens (Train) - Problem Fact
 * 
 * Represents a train with its passenger capacity.
 * This is a problem fact that Timefold uses as input data.
 * The planning variable (vilciensId in AtiešanasLaiks) will reference this train's ID.
 */
public class Vilciens {
    
    private Long id;
    private int kapacitate; // Passenger capacity
    
    // No-arg constructor required by Timefold
    public Vilciens() {
    }
    
    public Vilciens(Long id, int kapacitate) {
        this.id = id;
        this.kapacitate = kapacitate;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getKapacitate() {
        return kapacitate;
    }
    
    public void setKapacitate(int kapacitate) {
        this.kapacitate = kapacitate;
    }
    
    @Override
    public String toString() {
        return "Vilciens{" +
                "id=" + id +
                ", kapacitate=" + kapacitate +
                '}';
    }
}
