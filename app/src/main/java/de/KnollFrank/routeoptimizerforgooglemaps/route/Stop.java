package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public record Stop(String id,
                   String address,
                   // FK-TODO: brauchen neue Klasse PlaceId mit Methoden toWebFormat() und toHexFormat()
                   Optional<String> placeId,
                   Geodetic geodetic,
                   Optional<DeliveryGroup> deliveryGroup) {
}
