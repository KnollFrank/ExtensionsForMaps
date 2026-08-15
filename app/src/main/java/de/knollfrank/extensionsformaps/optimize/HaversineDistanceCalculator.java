package de.knollfrank.extensionsformaps.optimize;

import de.knollfrank.extensionsformaps.route.Stop;

class HaversineDistanceCalculator {

    public static double calculateDistance(final Stop from, final Stop to) {
        return calculateHaversineDistance(
                from.geodetic().getLatitude().toDegrees(),
                from.geodetic().getLongitude().toDegrees(),
                to.geodetic().getLatitude().toDegrees(),
                to.geodetic().getLongitude().toDegrees());
    }

    private static double calculateHaversineDistance(final double lat1,
                                                     final double lon1,
                                                     final double lat2,
                                                     final double lon2) {
        final double R = 6371000.0; // Earth's radius in meters
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
