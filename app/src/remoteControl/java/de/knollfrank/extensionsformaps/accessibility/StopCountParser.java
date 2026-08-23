package de.knollfrank.extensionsformaps.accessibility;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StopCountParser {

    private final Pattern stopCountPattern;

    public StopCountParser(final Pattern stopCountPattern) {
        this.stopCountPattern = stopCountPattern;
    }

    public OptionalInt parseStopCount(final String text) {
        final Matcher matcher = stopCountPattern.matcher(text);
        if (matcher.find()) {
            try {
                final Optional<String> group = Optional.ofNullable(matcher.group(1));
                if (group.isPresent()) {
                    return OptionalInt.of(Integer.parseInt(group.orElseThrow()));
                }
            } catch (final NumberFormatException ignored) {
            }
        }
        return OptionalInt.empty();
    }
}
