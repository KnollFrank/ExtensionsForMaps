package de.KnollFrank.routeoptimizerforgooglemaps.common;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class Optionals {

	private Optionals() {
	}

	public static <T> Set<T> asSet(final Optional<T> optional) {
		return optional.stream().collect(Collectors.toUnmodifiableSet());
	}

	public static Optional<Integer> asOptional(final OptionalInt optionalInt) {
		return optionalInt.stream().boxed().findFirst();
	}
}
