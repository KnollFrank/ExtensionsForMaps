package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class Strings {

    private Strings() {
    }

    public static OptionalInt indexOf(final String haystack, final String needle, final int fromIndex) {
        return IndexSearchResultConverter.minusOneToEmpty(haystack.indexOf(needle, fromIndex));
    }

    public static OptionalInt indexOf(final String haystack, final String needle) {
        return indexOf(haystack, needle, 0);
    }

    public static List<String> split(final String str, final Pattern regex) {
        return List.of(regex.split(str));
    }
}
