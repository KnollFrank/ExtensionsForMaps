package de.knollfrank.extensionsformaps.accessibility;

import java.util.OptionalInt;

public class GoogleMapsContext {

    public final String addStopsText;
    public final String stopsWord;
    public final String shareText;
    private final StopCountParser stopCountParser;

    public GoogleMapsContext(final String addStopsText,
                             final String stopsWord,
                             final String shareText,
                             final StopCountParser stopCountParser) {
        this.addStopsText = addStopsText;
        this.stopsWord = stopsWord;
        this.shareText = shareText;
        this.stopCountParser = stopCountParser;
    }

    public OptionalInt parseStopCount(final String text) {
        return stopCountParser.parseStopCount(text);
    }
}
