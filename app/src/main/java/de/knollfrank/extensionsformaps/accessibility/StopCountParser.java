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
        return this
                .parseStopCountAsStr(text)
                .map(StopCountParser::parseInt)
                .orElse(OptionalInt.empty());
    }

    private Optional<String> parseStopCountAsStr(final String text) {
        final Matcher matcher = stopCountPattern.matcher(text);
        return matcher.find() ?
                Optional.ofNullable(matcher.group(1)) :
                Optional.empty();
    }

    private static OptionalInt parseInt(final String str) {
        try {
            return OptionalInt.of(Integer.parseInt(str));
        } catch (final NumberFormatException ignored) {
        }
        return OptionalInt.empty();
    }
}
