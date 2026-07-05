package de.KnollFrank.routeoptimizerforgooglemaps.common;

import com.google.common.collect.Range;

import java.util.Optional;

public class Ranges {

    private Ranges() {
    }

    public static <T extends Comparable> Optional<? extends T> getEndpoint(final Range<T> range,
                                                                           final boolean lower) {
        return lower ? getLowerEndpoint(range) : getUpperEndpoint(range);
    }

    private static <T extends Comparable> Optional<T> getLowerEndpoint(final Range<T> range) {
        return range.hasLowerBound() ?
                Optional.of(range.lowerEndpoint()) :
                Optional.empty();
    }

    private static <T extends Comparable> Optional<T> getUpperEndpoint(final Range<T> range) {
        return range.hasUpperBound() ?
                Optional.of(range.upperEndpoint()) :
                Optional.empty();
    }
}
