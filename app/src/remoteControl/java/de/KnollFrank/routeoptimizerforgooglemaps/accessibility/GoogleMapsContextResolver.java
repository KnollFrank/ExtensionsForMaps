package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.regex.Pattern;

public class GoogleMapsContextResolver {

    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String KEY_ADD_STOPS = "ADD_STOPS_ENTRYPOINT_LABEL";
    private static final String KEY_COUNT_STOPS = "DIRECTIONS_COUNT_STOPS";

    public static GoogleMapsContext resolve(final Context context) {
        try {
            return _resolve(getGoogleMapsContext(context));
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Google Maps package not found", e);
        }
    }

    private static Context getGoogleMapsContext(final Context context) throws PackageManager.NameNotFoundException {
        return context.createPackageContext(GOOGLE_MAPS_PACKAGE, 0);
    }

    private static GoogleMapsContext _resolve(final Context context) {
        final Resources resources = context.getResources();
        final String countStopsPatternStr = getCountStopsPatternStr(resources);
        return new GoogleMapsContext(
                getAddStopsText(resources),
                getStopsWord(countStopsPatternStr),
                getStopCountPattern(countStopsPatternStr));
    }

    private static String getAddStopsText(final Resources resources) {
        final int addStopsId = resources.getIdentifier(KEY_ADD_STOPS, "string", GOOGLE_MAPS_PACKAGE);
        if (addStopsId == 0) {
            throw new RuntimeException("Could not find resource ID for " + KEY_ADD_STOPS);
        }
        return resources.getString(addStopsId);
    }

    private static String getCountStopsPatternStr(final Resources resources) {
        final int countStopsId = resources.getIdentifier(KEY_COUNT_STOPS, "plurals", GOOGLE_MAPS_PACKAGE);
        if (countStopsId == 0) {
            throw new RuntimeException("Could not find resource ID for " + KEY_COUNT_STOPS);
        }
        return resources.getQuantityString(countStopsId, 5);
    }

    private static String getStopsWord(final String countStopsPatternStr) {
        return countStopsPatternStr
                .replace("%d", "")
                .replace("%1$d", "")
                .replaceAll("\\s+", "");
    }

    private static Pattern getStopCountPattern(final String countStopsPatternStr) {
        return Pattern.compile(
                countStopsPatternStr
                        .replace("%d", "(\\d+)")
                        .replace("%1$d", "(\\d+)"));
    }
}
