package de.knollfrank.extensionsformaps.common;

import java.util.Arrays;
import java.util.function.Supplier;

public class Booleans {

    private Booleans() {
    }

    @SafeVarargs
    public static boolean executeUntilFirstIsTrue(final Supplier<Boolean>... elements) {
        return Arrays
                .stream(elements)
                .map(Supplier::get)
                .filter(Boolean::booleanValue)
                .findFirst()
                .orElse(false);
    }
}
