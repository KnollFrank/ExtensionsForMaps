package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Objects;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public record Stop(String id,
                   String address,
                   Optional<String> placeId,
                   Geodetic geodetic,
                   // FK-TODO: make OptionalInt because Route.origin and Route.destination do not have priorities.
                   int priority) {

    public Stop(final String id,
                final String address,
                final Optional<String> placeId,
                final Geodetic geodetic) {
        this(id, address, placeId, geodetic, 2);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Stop stop = (Stop) o;
        return Objects.equals(id, stop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
