package de.knollfrank.extensionsformaps.accessibility;

import java.util.OptionalInt;

public final class GoogleMapsContext {

    public final String addStopsText;
    public final String stopsWord;
    private final StopCountParser stopCountParser;

    public GoogleMapsContext(final String addStopsText,
                             final String stopsWord,
                             final StopCountParser stopCountParser) {
        this.addStopsText = addStopsText;
        this.stopsWord = stopsWord;
        this.stopCountParser = stopCountParser;
    }

    public OptionalInt parseStopCount(final String text) {
        return stopCountParser.parseStopCount(text);
    }
}
