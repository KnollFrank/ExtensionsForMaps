package de.KnollFrank.routeoptimizerforgooglemaps.route;

import com.google.common.collect.ImmutableList;

import java.util.List;

// FK-TODO: in Google Maps dürfen dieselben zwei Stops (mit derselben Adresse) niemals direkt aufeinanderfolgen. Soll das hier im Konstruktor sichergestellt werden?
public record Route(Stop origin, List<Stop> waypoints, Stop destination) {

    public List<Stop> stops() {
        return ImmutableList
                .<Stop>builder()
                .add(origin)
                .addAll(waypoints)
                .add(destination)
                .build();
    }
}
