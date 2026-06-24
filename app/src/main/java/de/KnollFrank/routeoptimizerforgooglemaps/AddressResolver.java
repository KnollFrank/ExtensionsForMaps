package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressResolver {

    // FK-FEATURE: Geocoder soll nur eine Möglichkeit von mehreren sein, darunter https://github.com/komoot/photon
    private final Geocoder geocoder;

    public AddressResolver(final Context context) {
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    public List<RouteOptimizer.Stop> resolveCoordinatesForStops(final List<RouteOptimizer.Stop> stops) {
        final List<RouteOptimizer.Stop> completeStops = new ArrayList<>();
        for (final RouteOptimizer.Stop stop : stops) {
            if (stop.lat() != 0 || stop.lng() != 0) {
                completeStops.add(stop);
            } else {
                try {
                    final List<Address> addresses = geocoder.getFromLocationName(stop.address(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        completeStops.add(
                                new RouteOptimizer.Stop(
                                        stop.address(),
                                        addresses.get(0).getLatitude(),
                                        addresses.get(0).getLongitude()));
                    } else {
                        completeStops.add(stop);
                    }
                } catch (final IOException e) {
                    completeStops.add(stop);
                }
            }
        }
        return completeStops;
    }

    public List<RouteOptimizer.Stop> resolveAddressesToStops(final List<String> addressList) throws IOException {
        final List<RouteOptimizer.Stop> stops = new ArrayList<>();
        if (addressList.isEmpty()) {
            return stops;
        }

        final String startAddressStr = addressList.get(0);
        final List<Address> startCoords = geocoder.getFromLocationName(startAddressStr, 1);
        if (startCoords == null || startCoords.isEmpty()) {
            throw new IOException("Could not find start location: " + startAddressStr);
        }

        final Address startLocation = startCoords.get(0);
        stops.add(
                new RouteOptimizer.Stop(
                        startAddressStr,
                        startLocation.getLatitude(),
                        startLocation.getLongitude()));

        for (int i = 1; i < addressList.size(); i++) {
            final String addressStr = addressList.get(i);
            try {
                final List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    final Address addr = addresses.get(0);
                    stops.add(
                            new RouteOptimizer.Stop(
                                    addressStr,
                                    addr.getLatitude(),
                                    addr.getLongitude()));
                } else {
                    stops.add(new RouteOptimizer.Stop(addressStr, 0.0, 0.0));
                }
            } catch (final IOException e) {
                stops.add(new RouteOptimizer.Stop(addressStr, 0.0, 0.0));
            }
        }
        return stops;
    }
}