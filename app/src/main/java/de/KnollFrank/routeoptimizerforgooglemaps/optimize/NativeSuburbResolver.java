package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public class NativeSuburbResolver {

    private final Geocoder geocoder;

    public NativeSuburbResolver(final Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public Optional<String> resolveSuburb(final Geodetic geodetic) {
        return this
                .resolveAddress(geodetic)
                .map(NativeSuburbResolver::getSuburb);
    }

    private Optional<Address> resolveAddress(final Geodetic geodetic) {
        try {
            return Optional
                    .ofNullable(
                            geocoder.getFromLocation(
                                    geodetic.getLatitude().toDegrees(),
                                    geodetic.getLongitude().toDegrees(),
                                    1))
                    .flatMap(addresses -> addresses.isEmpty() ? Optional.empty() : Optional.of(addresses.get(0)));
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static String getSuburb(final Address address) {
        return address.getSubLocality();
    }
}