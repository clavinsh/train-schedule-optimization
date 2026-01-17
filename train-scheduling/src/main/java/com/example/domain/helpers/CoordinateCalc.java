package com.example.domain.helpers;

public final class CoordinateCalc {
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double haversineDistance(double lat_1, double long_1, double lat_2,
            double long_2) {
        double lat_1Rad = Math.toRadians(lat_1);
        double lat_2Rad = Math.toRadians(lat_2);
        double deltaLatRad = Math.toRadians(lat_2 - lat_1);
        double deltaLonRad = Math.toRadians(long_2 - long_1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) + Math.cos(lat_1Rad)
                * Math.cos(lat_2Rad) * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
