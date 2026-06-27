package de.KnollFrank.routeoptimizerforgooglemaps.route;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;

public class RouteToUrlConverter {

    // FK-TODO: add unit test for building the URL.
    public static String getUrl(final Route route) {
        // 1. The modern, official Google Maps Directions API endpoint
        final StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");

        // 2. Set the exact coordinates for the starting point
        final Stop origin = Lists.getHead(route.stops()).orElseThrow();
        // FK-TODO: add origin_place_id
        urlBuilder
                .append("&origin=")
                .append(origin.geodetic().getLatitude().toDegrees())
                .append(",")
                .append(origin.geodetic().getLongitude().toDegrees());

        // 3. Set the exact coordinates for the final destination
        final Stop destination = Lists.getLastElement(route.stops()).orElseThrow();
        // FK-TODO: add destination_place_id
        urlBuilder
                .append("&destination=")
                .append(destination.geodetic().getLatitude().toDegrees())
                .append(",")
                .append(destination.geodetic().getLongitude().toDegrees());

        // 4. Handle intermediate waypoints if there are any stops in between
        if (route.stops().size() > 2) {
            // FK-TODO: add waypoint_place_ids
            urlBuilder.append("&waypoints=");
            for (int i = 1; i < route.stops().size() - 1; i++) {
                final Stop waypoint = route.stops().get(i);
                urlBuilder
                        .append(waypoint.geodetic().getLatitude().toDegrees())
                        .append(",")
                        .append(waypoint.geodetic().getLongitude().toDegrees());
                // Modern API separates multiple waypoints using the pipe character '|'
                if (i < route.stops().size() - 2) {
                    urlBuilder.append("|");
                }
            }
        }
        return urlBuilder.toString();
    }
}
