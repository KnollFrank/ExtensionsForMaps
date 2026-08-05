package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

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
        final String query = url.getQuery();
        if (query == null) return List.of();

        final java.util.Map<String, String> params = URLs.parseQuery(query);
        final List<String> addresses = new java.util.ArrayList<>();

        // Startadresse
        final String saddr = params.get("saddr");
        if (saddr != null) addresses.add(URLs.decode(saddr));

        // Zieladressen (können mehrere sein, getrennt durch " to:")
        final String daddr = params.get("daddr");
        if (daddr != null) {
            final String decodedDaddr = URLs.decode(daddr);
            // Google nutzt " to:" als Trenner für Zwischenstopps im Legacy-Format
            addresses.addAll(List.of(decodedDaddr.split(" to:")));
        }

        return addresses;
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
