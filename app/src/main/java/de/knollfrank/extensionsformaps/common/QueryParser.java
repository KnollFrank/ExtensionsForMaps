package de.knollfrank.extensionsformaps.common;

import android.net.Uri;
import android.net.Uri.Builder;

import com.google.common.collect.ImmutableMap;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class QueryParser {

    public static Map<String, String> parseQuery(final URL url) {
        return QueryParser
                .getQueryAsUri(url)
                .map(QueryParser::parseQuery)
                .orElse(ImmutableMap.of());
    }

    private static Optional<Uri> getQueryAsUri(final URL url) {
        return Optional
                .ofNullable(url.getQuery())
                .map(query ->
                        new Builder()
                                .encodedQuery(query)
                                .build());
    }

    private static Map<String, String> parseQuery(final Uri uri) {
        return uri
                .getQueryParameterNames()
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Function.identity(),
                                uri::getQueryParameter));
    }
}
