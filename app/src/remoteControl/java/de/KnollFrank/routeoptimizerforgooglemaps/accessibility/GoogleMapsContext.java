package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import java.util.regex.Pattern;

public record GoogleMapsContext(String addStopsText,
                                String stopsWord,
                                Pattern stopCountPattern) {
}
