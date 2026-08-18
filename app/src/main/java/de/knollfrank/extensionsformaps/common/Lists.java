package de.knollfrank.extensionsformaps.common;

import androidx.core.util.Pair;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Lists {

    private Lists() {
    }

    public static <T> List<T> concat(final List<List<T>> lists) {
        return lists
                .stream()
                .flatMap(List::stream)
                .toList();
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

    public static <T> Optional<InitAndLast<T>> asInitAndLast(final List<T> ts) {
        return ts.isEmpty() ?
                Optional.empty() :
                Optional.of(
                        new InitAndLast<>(
                                getInit(ts).orElseThrow(),
                                getLast(ts).orElseThrow()));
    }

    public static <T> Optional<T> getElementAtIndex(final List<T> ts, final int index) {
        return 0 <= index && index < ts.size() ?
                Optional.of(ts.get(index)) :
                Optional.empty();
    }

    public static <T> Optional<T> getHead(final List<T> ts) {
        return getElementAtIndex(ts, 0);
    }

    public static <T> Optional<List<T>> getTail(final List<T> ts) {
        return ts.isEmpty() ?
                Optional.empty() :
                Optional.of(ts.subList(1, ts.size()));
    }

    public static <T> Optional<T> getLast(final List<T> ts) {
        return getElementAtIndex(ts, ts.size() - 1);
    }

    public static <T> Optional<List<T>> getInit(final List<T> ts) {
        return ts.isEmpty() ?
                Optional.empty() :
                Optional.of(ts.subList(0, ts.size() - 1));
    }

    public static <T> List<T> concat(final T head, final List<T> tail) {
        return ImmutableList
                .<T>builder()
                .add(head)
                .addAll(tail)
                .build();
    }

    public static <T> List<T> concat(final List<T> ts, final T t) {
        return ImmutableList
                .<T>builder()
                .addAll(ts)
                .add(t)
                .build();
    }

    public static <T> List<T> distinct(final List<T> ts) {
        return ts.stream().distinct().toList();
    }

    // adapted from https://stackoverflow.com/questions/31963297/how-to-zip-two-java-lists
    public static <A, B> List<Pair<A, B>> zip(final List<A> as, final List<B> bs) {
        if (as.size() != bs.size()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Lists must have the same size to be zipped. List 1 (size %d): %s, List 2 (size %d): %s",
                            as.size(),
                            as,
                            bs.size(),
                            bs));
        }
        return IntStream
                .range(0, as.size())
                .mapToObj(i -> Pair.create(as.get(i), bs.get(i)))
                .toList();
    }

    public static <A, B> Pair<List<A>, List<B>> truncateToCommonSize(final List<A> as, final List<B> bs) {
        final int size = Math.min(as.size(), bs.size());
        return Pair.create(as.subList(0, size), bs.subList(0, size));
    }

    public static List<Integer> createRange(final int startInclusive, final int endInclusive) {
        return IntStream
                .rangeClosed(startInclusive, endInclusive)
                .boxed()
                .toList();
    }
}
