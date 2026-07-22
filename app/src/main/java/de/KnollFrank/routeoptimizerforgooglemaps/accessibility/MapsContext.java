package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import java.util.regex.Pattern;

public record MapsContext(String addStopsText,
                          String stopsWord,
                          Pattern stopCountPattern) {
}
