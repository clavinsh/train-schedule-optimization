package org.acme.rollingstockrostering.domain;

import java.util.Objects;

/**
 * GeoCoordinates - Value object for geographic coordinates
 *
 * Represents a point on Earth using latitude and longitude.
 * Provides distance calculation using the Haversine formula.
 */
public class GeoCoordinates {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private double latitude;
    private double longitude;

    public GeoCoordinates() {
    }

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Calculate distance to another point using Haversine formula.
     *
     * @param other the other coordinates
     * @return distance in kilometers
     */
    public double distanceTo(GeoCoordinates other) {
        if (other == null) {
            return 0.0;
        }

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimate travel time in minutes assuming average train speed.
     *
     * @param other the destination coordinates
     * @param averageSpeedKmh average train speed in km/h
     * @return estimated travel time in minutes
     */
    public long estimateTravelTimeMinutes(GeoCoordinates other, double averageSpeedKmh) {
        double distanceKm = distanceTo(other);
        double hours = distanceKm / averageSpeedKmh;
        return Math.round(hours * 60);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoCoordinates that = (GeoCoordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
