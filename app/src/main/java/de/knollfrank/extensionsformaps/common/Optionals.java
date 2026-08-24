package de.knollfrank.extensionsformaps.common;

import androidx.core.util.Pair;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Optionals {

    private Optionals() {
    }

    @SafeVarargs
    public static <T> Stream<T> streamOfPresentElements(final Supplier<Optional<? extends T>>... elements) {
        return Arrays
                .stream(elements)
                .map(Supplier::get)
                .flatMap(Optional::stream);
    }

    public static <T> Set<T> asSet(final Optional<T> optional) {
        return optional.stream().collect(Collectors.toUnmodifiableSet());
    }

    public static Optional<Integer> asOptional(final OptionalInt optionalInt) {
        return optionalInt.stream().boxed().findFirst();
    }

    public static <A, B> void ifPresentBoth(final Pair<Optional<A>, Optional<B>> optA_optB, final BiConsumer<A, B> consumer) {
        optA_optB.first.ifPresent(a -> optA_optB.second.ifPresent(b -> consumer.accept(a, b)));
    }
}
