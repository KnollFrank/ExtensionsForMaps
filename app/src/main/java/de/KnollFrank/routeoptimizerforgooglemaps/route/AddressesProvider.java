package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Optionals;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Strings;
import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

class AddressesProvider {

    public static List<String> getUrlDecodedAddresses(final URL directionsUrl) {
        return URLs.decode(
                Strings.split(
                        getPathPart(directionsUrl.getPath()),
                        "/"));
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
