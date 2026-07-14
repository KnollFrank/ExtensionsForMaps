package de.KnollFrank.routeoptimizerforgooglemaps.route;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public record Stop(String id,
                   String address,
                   Geodetic geodetic) {
}
