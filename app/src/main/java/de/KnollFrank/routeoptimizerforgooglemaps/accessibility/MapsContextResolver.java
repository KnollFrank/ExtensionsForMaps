package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.regex.Pattern;

public class MapsContextResolver {

    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String KEY_ADD_STOPS = "ADD_STOPS_ENTRYPOINT_LABEL";
    private static final String KEY_COUNT_STOPS = "DIRECTIONS_COUNT_STOPS";

    // FK-TODO: refactor
    public static MapsContext resolve(final Context context) {
        try {
            final Context mapsContext = context.createPackageContext(MAPS_PACKAGE, 0);
            final Resources mapsRes = mapsContext.getResources();

            final int addStopsId = mapsRes.getIdentifier(KEY_ADD_STOPS, "string", MAPS_PACKAGE);
            if (addStopsId == 0) {
                throw new RuntimeException("Could not find resource ID for " + KEY_ADD_STOPS);
            }
            final String addStopsText = mapsRes.getString(addStopsId);

            final int countStopsId = mapsRes.getIdentifier(KEY_COUNT_STOPS, "plurals", MAPS_PACKAGE);
            if (countStopsId == 0) {
                throw new RuntimeException("Could not find resource ID for " + KEY_COUNT_STOPS);
            }
            final String patternStr = mapsRes.getQuantityString(countStopsId, 5);
            final String stopsWord = patternStr.replace("%d", "").replace("%1$d", "").trim();
            final String regex = patternStr.replace("%d", "(\\d+)").replace("%1$d", "(\\d+)");
            final Pattern stopCountPattern = Pattern.compile(regex);
            return new MapsContext(addStopsText, stopsWord, stopCountPattern);
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Google Maps package not found", e);
        }
    }
}
