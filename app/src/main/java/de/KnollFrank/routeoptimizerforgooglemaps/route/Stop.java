package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public record Stop(int stopNumber,
                   String address,
                   Optional<String> placeId,
                   Geodetic geodetic) {
}
