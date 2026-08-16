package de.knollfrank.extensionsformaps.route.url;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.URLs;

// FK-TODO: add unit test
class LegacyAddressesProvider {

    public static List<String> getUrlDecodedAddresses(final LegacyDirectionsUrl legacyDirectionsUrl) {
        return getLegacyAddresses(URLs.getQuery(legacyDirectionsUrl.url()));
    }

    private static ImmutableList<String> getLegacyAddresses(final ImmutableMap<String, String> params) {
        final ImmutableList.Builder<String> addressesBuilder = ImmutableList.builder();
        // Startadresse
        Maps
                .get(params, "saddr")
                .ifPresent(addressesBuilder::add);
        // Zieladressen (können mehrere sein, getrennt durch " to:")
        Maps
                .get(params, "daddr")
                .ifPresent(
                        daddr ->
                                // Google nutzt " to:" als Trenner für Zwischenstopps im Legacy-Format
                                addressesBuilder.addAll(List.of(daddr.split(" to:"))));
        return addressesBuilder.build();
    }
}
