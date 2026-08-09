package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // FK-TODO: refactor
    private static List<String> getLegacyAddresses(final URL url) {
        final Map<String, String> params = URLs.parseQuery(url);
        final List<String> addresses = new ArrayList<>();

        // Startadresse (already decoded by URLs.parseQuery)
        final String saddr = params.get("saddr");
        if (saddr != null) addresses.add(saddr);

        // Zieladressen (können mehrere sein, getrennt durch " to:")
        final String daddr = params.get("daddr");
        if (daddr != null) {
            // Google nutzt " to:" als Trenner für Zwischenstopps im Legacy-Format
            addresses.addAll(List.of(daddr.split(" to:")));
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
