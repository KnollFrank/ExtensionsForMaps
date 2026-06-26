package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Lists {

    private Lists() {
    }

    public static <T> List<IndexedElement<T>> asIndexedElements(final List<T> elements) {
        return IntStream
                .range(0, elements.size())
                .mapToObj(index -> new IndexedElement<>(index, elements.get(index)))
                .toList();
    }

    public static <T> Optional<HeadAndTail<T>> asHeadAndTail(final List<T> ts) {
        return ts.isEmpty() ?
                Optional.empty() :
                Optional.of(
                        new HeadAndTail<>(
                                getHead(ts).orElseThrow(),
                                getTail(ts).orElseThrow()));
    }

    public static <T> Optional<T> getElementAtIndex(final List<T> ts, final int index) {
        return 0 <= index && index < ts.size() ?
                Optional.of(ts.get(index)) :
                Optional.empty();
    }

    public static <T> Optional<T> getHead(final List<T> ts) {
        return getElementAtIndex(ts, 0);
    }

    public static <T> Optional<T> getLastElement(final List<T> ts) {
        return getElementAtIndex(ts, ts.size() - 1);
    }

    public static <T> Optional<List<T>> getTail(final List<T> ts) {
        return ts.isEmpty() ?
                Optional.empty() :
                Optional.of(ts.subList(1, ts.size()));
    }
}
