package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.Arrays;
import java.util.List;

public class Lists {

    private Lists() {
    }

    public static <T> List<T> toList(final T[] ts) {
        return Arrays.stream(ts).toList();
    }
}
