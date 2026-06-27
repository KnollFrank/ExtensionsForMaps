package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

// FK-FEATURE: biete neben der HaversineDistance auch folgendes an:
//  + Embedded GraphHopper + GitHub-Download
//  - OpenRouteService-API (https://openrouteservice.org/)
//  - https://locationiq.com/
public enum OptimizationStrategy {
    HAVERSINE,
    OSRM
}
