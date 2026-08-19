package de.knollfrank.extensionsformaps.common;

import android.net.Uri;

import com.google.common.collect.ImmutableMap;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class URLs {

    private URLs() {
    }

    public static ImmutableMap<String, String> getQuery(final URL url) {
        return QueryProvider.getQuery(url);
    }

    public static URL createUrl(final Uri uri) {
        return createUrl(uri.toString());
    }

    public static URL createUrl(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Uri createUri(final URL url) {
        return Uri.parse(url.toString());
    }

    public static String encode(final String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decode(final String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> decode(final List<String> strs) {
        return strs
                .stream()
                .map(URLs::decode)
                .toList();
    }
}
