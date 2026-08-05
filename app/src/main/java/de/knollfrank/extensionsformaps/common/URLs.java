package de.knollfrank.extensionsformaps.common;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLs {

    private URLs() {
    }

    public static Map<String, String> parseQuery(final String query) {
        final Map<String, String> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        for (final String param : query.split("&")) {
            final String[] entry = param.split("=");
            if (entry.length > 1) {
                params.put(entry[0], entry[1]);
            }
        }
        return params;
    }

    public static URL createUrl(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
