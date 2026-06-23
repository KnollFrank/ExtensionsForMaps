package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Lists {

	private Lists() {
	}

	public record IndexedElement<T>(int index, T element) {
	}

	public static <T> List<IndexedElement<T>> asIndexedElements(final List<T> ts) {
		return IntStream
				.range(0, ts.size())
				.mapToObj(index -> new IndexedElement<>(index, ts.get(index)))
				.toList();
	}

	public static <T> List<T> toList(final T[] ts) {
		return Arrays.stream(ts).toList();
	}
}
