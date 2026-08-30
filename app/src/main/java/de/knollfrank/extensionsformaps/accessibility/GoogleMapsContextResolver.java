package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_MAPS_PACKAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.knollfrank.extensionsformaps.common.ResourcesWrapper;

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

    private static String getAddStopsText(final Resources resources) {
        return resources.getString(
                new ResourcesWrapper(resources)
                        .getValidIdentifierOrElseThrow(
                                "ADD_STOPS_ENTRYPOINT_LABEL",
                                "string",
                                GOOGLE_MAPS_PACKAGE));
    }

    private static String getStopsWord(final String countStopsPatternStr) {
        return countStopsPatternStr
                .replace("%d", "")
                .replace("%1$d", "")
                .replaceAll("\\s+", "");
    }

    private static String getShareText(final Resources resources) {
        final String keyShareButton = "ACCESSIBILITY_SHARE_BUTTON";
        final String keyShareFallback = "SHARE";
        return resources.getString(
                Stream
                        .of(keyShareButton, keyShareFallback)
                        .map(key -> new ResourcesWrapper(resources).getIdentifier(key, "string", PackageNames.GOOGLE_MAPS_PACKAGE))
                        .flatMap(optionalIdentifier -> optionalIdentifier.stream().boxed())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Could not find resource ID for share button in Google Maps")));
    }

    private static String getCountStopsPatternStr(final Resources resources) {
        return resources.getQuantityString(
                new ResourcesWrapper(resources)
                        .getValidIdentifierOrElseThrow(
                                "DIRECTIONS_COUNT_STOPS",
                                "plurals",
                                GOOGLE_MAPS_PACKAGE),
                5);
    }

    private static Pattern getStopCountPattern(final String countStopsPatternStr) {
        return Pattern.compile(
                countStopsPatternStr
                        .replace("%d", "(\\d+)")
                        .replace("%1$d", "(\\d+)"));
    }
}
