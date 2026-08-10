package de.knollfrank.extensionsformaps.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.common.Strings;
import de.knollfrank.extensionsformaps.common.URLs;

// FK-TODO: add unit test
class AddressesProvider {

    public static List<String> getUrlDecodedAddresses(final URL directionsUrl) {
        if (DirectionsUrlPredicate.isModernDirectionsUrl(directionsUrl)) {
            return URLs.decode(getPathParts(directionsUrl));
        } else if (DirectionsUrlPredicate.isLegacyDirectionsUrl(directionsUrl)) {
            return getLegacyAddresses(directionsUrl);
        }
        return List.of();
    }

    private static List<String> getLegacyAddresses(final URL url) {
        return getLegacyAddresses(URLs.parseQuery(url));
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

    private static List<String> getPathParts(final URL directionsUrl) {
        return Strings.split(
                getPathPart(directionsUrl.getPath()),
                Pattern.compile("/"));
    }

    private static String getPathPart(final String path) {
        return path.substring(getStartIndex(path), getEndIndex(path));
    }

    private static int getStartIndex(final String path) {
        final String dirPathSegment = "/dir/";
        return Strings.indexOf(path, dirPathSegment).orElseThrow() + dirPathSegment.length();
    }

    private static int getEndIndex(final String path) {
        final int endIndex =
                Strings
                        .indexOf(path, "/data=")
                        .orElseGet(path::length);
        return Optionals
                .asOptional(Strings.indexOf(path, "/@"))
                .map(indexOfAddSegment -> Math.min(endIndex, indexOfAddSegment))
                .orElse(endIndex);
    }
}
