package org.acme.rollingstockrostering.domain;

public class GeoCoordinates {
    private double latitude;
    private double longitude;

    public GeoCoordinates() {}

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Calculate distance to another coordinate using Haversine formula
     * @return distance in kilometers
     */
    public double distanceKm(GeoCoordinates other) {
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(this.latitude)) * 
                   Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Getters and setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
