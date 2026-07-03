package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.Objects;

public record DeliveryGroup(String id, String name, int sequenceOrder) {

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final DeliveryGroup that = (DeliveryGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
