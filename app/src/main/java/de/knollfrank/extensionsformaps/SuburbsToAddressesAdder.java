package de.knollfrank.extensionsformaps;

import android.content.Context;

import java.util.List;

import de.knollfrank.extensionsformaps.optimize.NativeSuburbResolver;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

class SuburbsToAddressesAdder {

    private final NativeSuburbResolver nativeSuburbResolver;
    private final Context context;

    public SuburbsToAddressesAdder(final NativeSuburbResolver nativeSuburbResolver, final Context context) {
        this.nativeSuburbResolver = nativeSuburbResolver;
        this.context = context;
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

    private Stop addSuburbToAddress(final Stop stop, final String suburb) {
        return new Stop(
                stop.id(),
                addSuburbToAddress(stop.address(), suburb),
                stop.officialPlaceId(),
                stop.geodetic(),
                stop.deliveryGroup());
    }

    private String addSuburbToAddress(final String address, final String suburb) {
        return context.getString(R.string.address_with_suburb_format, address, suburb);
    }
}
