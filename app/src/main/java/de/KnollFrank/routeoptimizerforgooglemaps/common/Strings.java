package de.KnollFrank.routeoptimizerforgooglemaps.common;

import static de.KnollFrank.routeoptimizerforgooglemaps.common.IndexSearchResultConverter.minusOneToEmpty;

import java.util.List;
import java.util.OptionalInt;

public class Strings {

    private Strings() {
    }

    public static OptionalInt indexOf(final String haystack, final String needle, final int fromIndex) {
        return minusOneToEmpty(haystack.indexOf(needle, fromIndex));
    }

    public static OptionalInt indexOf(final String haystack, final String needle) {
        return indexOf(haystack, needle, 0);
    }

    public static List<String> split(final String str, final String regex) {
        return Lists.toList(str.split(regex));
    }
}
