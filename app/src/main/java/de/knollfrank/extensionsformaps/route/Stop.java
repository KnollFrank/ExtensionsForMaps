package de.knollfrank.extensionsformaps.route;

import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Geodetic;

public record Stop(String id,
                   String address,
                   Optional<OfficialPlaceId> officialPlaceId,
                   Geodetic geodetic,
                   Optional<DeliveryGroup> deliveryGroup) {
}
