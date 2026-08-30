package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.regex.Pattern;

public class GoogleMapsContextResolver {

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
                getShareText(resources),
                new StopCountParser(getStopCountPattern(countStopsPatternStr)));
    }

    private static String getShareText(final Resources resources) {
        final String keyShareButton = "ACCESSIBILITY_SHARE_BUTTON";
        final String keyShareFallback = "SHARE";
        int shareId = resources.getIdentifier(keyShareButton, "string", GOOGLE_MAPS_PACKAGE);
        if (shareId == 0) {
            shareId = resources.getIdentifier(keyShareFallback, "string", GOOGLE_MAPS_PACKAGE);
        }
        if (shareId == 0) {
            throw new RuntimeException("Could not find resource ID for share button in Google Maps");
        }
        return resources.getString(shareId);
    }

    private static String getCountStopsPatternStr(final Resources resources) {
        final String keyCountStops = "DIRECTIONS_COUNT_STOPS";
        final int countStopsId = resources.getIdentifier(keyCountStops, "plurals", GOOGLE_MAPS_PACKAGE);
        if (countStopsId == 0) {
            throw new RuntimeException("Could not find resource ID for " + keyCountStops);
        }
        return resources.getQuantityString(countStopsId, 5);
    }

    private static String getAddStopsText(final Resources resources) {
        final String keyAddStops = "ADD_STOPS_ENTRYPOINT_LABEL";
        final int addStopsId = resources.getIdentifier(keyAddStops, "string", GOOGLE_MAPS_PACKAGE);
        if (addStopsId == 0) {
            throw new RuntimeException("Could not find resource ID for " + keyAddStops);
        }
        return resources.getString(addStopsId);
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
