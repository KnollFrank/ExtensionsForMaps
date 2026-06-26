package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.List;
import java.util.stream.IntStream;

public class Lists {

    private Lists() {
    }

    public record IndexedElement<T>(int index, T element) {
    }

    public static <T> List<IndexedElement<T>> asIndexedElements(final List<T> elements) {
        return IntStream
                .range(0, elements.size())
                .mapToObj(index -> new IndexedElement<>(index, elements.get(index)))
                .toList();
    }
}
