package de.knollfrank.extensionsformaps.common;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
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

    public static <A, B> void ifPresentBoth(final Optional<A> optA, final Optional<B> optB, BiConsumer<A, B> consumer) {
        optA.ifPresent(a -> optB.ifPresent(b -> consumer.accept(a, b)));
    }
}
