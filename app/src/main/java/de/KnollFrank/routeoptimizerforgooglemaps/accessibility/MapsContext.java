package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record MapsContext(Set<String> localizedAddStopsTexts,
                          Set<String> localizedStopsWords,
                          List<Pattern> localizedStopCountPatterns) {
}
