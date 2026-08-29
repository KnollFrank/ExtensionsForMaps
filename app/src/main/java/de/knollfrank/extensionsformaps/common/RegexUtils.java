package de.knollfrank.extensionsformaps.common;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RegexUtils {

    private RegexUtils() {
    }

    public static Stream<MatchResult> toStream(final Matcher matcher) {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<>(
                        Long.MAX_VALUE,
                        Spliterator.ORDERED | Spliterator.NONNULL) {

                    @Override
                    public boolean tryAdvance(final Consumer<? super MatchResult> action) {
                        if (!matcher.find()) {
                            return false;
                        }
                        action.accept(matcher.toMatchResult());
                        return true;
                    }
                },
                false);
    }
}
