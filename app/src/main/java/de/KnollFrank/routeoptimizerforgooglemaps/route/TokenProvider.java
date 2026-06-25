package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class TokenProvider {

    private static final String DATA_PART_MARKER = "data=";

    public static Optional<List<String>> getTokensFromDataPart(final URL directionsUrl) {
        return TokenProvider
                .getDataPart(directionsUrl)
                .map(TokenProvider::getTokens);
    }

    private static Optional<String> getDataPart(final URL directionsUrl) {
        return directionsUrl.toString().contains(DATA_PART_MARKER) ?
                Optional.of(
                        directionsUrl
                        .toString()
                        .split(DATA_PART_MARKER)[1]
                        .split("&")[0]) :
                Optional.empty();
    }

    public static List<String> getTokens(final String dataPart) {
        final String delimiter = "!";
        return Arrays.asList(
                TokenProvider
                        .withoutDelimiterAtStart(dataPart, delimiter)
                        .split(delimiter));
    }

    private static String withoutDelimiterAtStart(final String str, final String delimiter) {
        return str.startsWith(delimiter) ?
                str.substring(delimiter.length()) :
                str;
    }
}
