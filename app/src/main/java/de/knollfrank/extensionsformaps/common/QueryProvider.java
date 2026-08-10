package de.knollfrank.extensionsformaps.common;

import android.net.Uri;
import android.net.Uri.Builder;

import com.google.common.collect.ImmutableMap;

import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

class QueryProvider {

    public static ImmutableMap<String, String> getQuery(final URL url) {
        return QueryProvider
                .getQueryAsUri(url)
                .map(QueryProvider::getQuery)
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

    private static ImmutableMap<String, String> getQuery(final Uri uri) {
        return uri
                .getQueryParameterNames()
                .stream()
                .collect(
                        ImmutableMap.toImmutableMap(
                                Function.identity(),
                                uri::getQueryParameter));
    }
}
