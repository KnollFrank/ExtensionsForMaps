package de.knollfrank.extensionsformaps.accessibility;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GoogleMapsContext(String addStopsText,
                                String stopsWord,
                                Pattern stopCountPattern) {

    public OptionalInt getStopCount(final String text) {
        final Matcher matcher = stopCountPattern.matcher(text);
        if (matcher.find()) {
            try {
                return OptionalInt.of(Integer.parseInt(matcher.group(1)));
            } catch (final NumberFormatException ignored) {
                return OptionalInt.empty();
            }
        }
        return OptionalInt.empty();
    }
}
