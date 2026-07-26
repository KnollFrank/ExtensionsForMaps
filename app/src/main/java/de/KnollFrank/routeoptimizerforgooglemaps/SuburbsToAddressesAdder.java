package de.KnollFrank.routeoptimizerforgooglemaps;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.NativeSuburbResolver;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

class SuburbsToAddressesAdder {

    private final NativeSuburbResolver nativeSuburbResolver;

    public SuburbsToAddressesAdder(final NativeSuburbResolver nativeSuburbResolver) {
        this.nativeSuburbResolver = nativeSuburbResolver;
    }


    public Route addSuburbsToAddresses(final Route route) {
        return new Route(
                addSuburbToAddress(route.origin()),
                addSuburbsToAddresses(route.waypoints()),
                addSuburbToAddress(route.destination()));
    }

    private List<Stop> addSuburbsToAddresses(final List<Stop> stops) {
        return stops
                .stream()
                .map(this::addSuburbToAddress)
                .toList();
    }

    private Stop addSuburbToAddress(final Stop stop) {
        return nativeSuburbResolver
                .resolveSuburb(stop.geodetic())
                .map(suburb -> addSuburbToAddress(stop, suburb))
                .orElse(stop);
    }

    private static Stop addSuburbToAddress(final Stop stop, final String suburb) {
        return new Stop(
                stop.id(),
                addSuburbToAddress(stop.address(), suburb),
                stop.officialPlaceId(),
                stop.geodetic(),
                stop.deliveryGroup());
    }

    private static String addSuburbToAddress(final String address, final String suburb) {
        return address + " (" + suburb + ")";
    }
}
