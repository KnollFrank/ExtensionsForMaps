package de.knollfrank.extensionsformaps.route.url;

import android.net.Uri;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.URLs;

class OfficialDirectionsUrlAddressesProvider {

    public static ImmutableList<String> getUrlDecodedAddresses(final OfficialDirectionsUrl officialDirectionsUrl) {
        return getUrlDecodedAddresses(URLs.createUri(officialDirectionsUrl.url()));
    }

    private static ImmutableList<String> getUrlDecodedAddresses(final Uri uri) {
        final ImmutableList.Builder<String> addressesBuilder = ImmutableList.builder();
        OfficialDirectionsUrlAddressesProvider
                .getNonEmptyQueryParameter(uri, "origin")
                .ifPresent(addressesBuilder::add);
        OfficialDirectionsUrlAddressesProvider
                .getNonEmptyQueryParameter(uri, "waypoints")
                .map(waypoints -> List.of(waypoints.split("\\|")))
                .ifPresent(addressesBuilder::addAll);
        OfficialDirectionsUrlAddressesProvider
                .getNonEmptyQueryParameter(uri, "destination")
                .ifPresent(addressesBuilder::add);
        return addressesBuilder.build();
    }

    private static Optional<String> getNonEmptyQueryParameter(final Uri uri, final String key) {
        return Optional
                .ofNullable(uri.getQueryParameter(key))
                .filter(value -> !value.isEmpty());
    }
}
