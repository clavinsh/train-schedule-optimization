package org.acme.rollingstockrostering.domain;

/**
 * GeoCoordinates - Simple DTO for geographic coordinates
 */
public class GeoCoordinates {
    
    private double latitude;
    private double longitude;
    
    public GeoCoordinates() {
    }
    
    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
