package de.knollfrank.extensionsformaps.accessibility;

import java.util.regex.Pattern;

public record GoogleMapsContext(String addStopsText,
                                String stopsWord,
                                Pattern stopCountPattern) {
}
