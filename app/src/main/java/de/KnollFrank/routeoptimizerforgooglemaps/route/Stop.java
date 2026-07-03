package de.KnollFrank.routeoptimizerforgooglemaps.route;

import com.google.common.collect.Range;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public record Stop(String id,
                   String address,
                   Optional<String> placeId,
                   Geodetic geodetic,
                   // FK-TODO: make deliveryGroup Optional
                   DeliveryGroup deliveryGroup,
                   Optional<Range<LocalDateTime>> timeWindow) {

    // FK-TODO: entferne alle überladenen Konstruktoren
    public Stop(final String id,
                final String address,
                final Optional<String> placeId,
                final Geodetic geodetic) {
        this(id,
             address,
             placeId,
             geodetic,
             DeliveryGroup.DEFAULT,
             Optional.empty());
    }

    public Stop(final String id,
                final String address,
                final Optional<String> placeId,
                final Geodetic geodetic,
                final Priority priority) {
        this(id,
             address,
             placeId,
             geodetic,
             new DeliveryGroup(
                     priority.name(),
                     priority.name(),
                     priority.priority),
             Optional.empty());
    }

    public Priority priority() {
        return Priority.fromPriority(deliveryGroup.sequenceOrder());
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
