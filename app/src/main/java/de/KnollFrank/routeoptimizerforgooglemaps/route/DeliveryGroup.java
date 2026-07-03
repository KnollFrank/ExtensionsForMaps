package de.KnollFrank.routeoptimizerforgooglemaps.route;

/**
 * Repräsentiert eine vom Boten im UI definierte Gruppe.
 */
public record DeliveryGroup(String id, String name, int sequenceOrder) {

    public static final DeliveryGroup DEFAULT = new DeliveryGroup("default", "Standard", 1);
}
