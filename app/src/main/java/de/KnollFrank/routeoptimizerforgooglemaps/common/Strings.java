package de.KnollFrank.routeoptimizerforgooglemaps.common;

import static de.KnollFrank.routeoptimizerforgooglemaps.common.IndexSearchResultConverter.minusOneToEmpty;

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
}
