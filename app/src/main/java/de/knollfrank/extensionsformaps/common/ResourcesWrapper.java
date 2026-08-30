package de.knollfrank.extensionsformaps.common;

import android.content.res.Resources;

import java.util.OptionalInt;

public record ResourcesWrapper(Resources resources) {

    public OptionalInt getIdentifier(final String name, final String defType, final String defPackage) {
        final int resourceId = resources.getIdentifier(name, defType, defPackage);
        return isValid(resourceId) ?
                OptionalInt.of(resourceId) :
                OptionalInt.empty();
    }

    public int getValidIdentifierOrElseThrow(final String name, final String defType, final String defPackage) {
        return this
                .getIdentifier(name, defType, defPackage)
                .orElseThrow(() -> new IllegalArgumentException("Could not find resource ID for " + name));
    }

    private static boolean isValid(final int resourceId) {
        return resourceId != 0;
    }
}
