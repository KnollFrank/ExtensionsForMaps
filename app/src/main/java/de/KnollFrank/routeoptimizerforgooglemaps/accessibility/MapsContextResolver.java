package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class MapsContextResolver {

    private static final String TAG = "MapsContextResolver";
    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";
    private static final String KEY_ADD_STOPS = "ADD_STOPS_ENTRYPOINT_LABEL";
    private static final String KEY_COUNT_STOPS = "DIRECTIONS_COUNT_STOPS";

    public static MapsContext resolve(final Context context) {
        final Set<String> localizedAddStopsTexts = new HashSet<>();
        final Set<String> localizedStopsWords = new HashSet<>();
        final List<Pattern> localizedStopCountPatterns = new ArrayList<>();

        try {
            final Context mapsContext = context.createPackageContext(MAPS_PACKAGE, 0);
            final String[] supportedLocales = mapsContext.getAssets().getLocales();

            for (final String localeTag : supportedLocales) {
                if (localeTag == null || localeTag.isEmpty()) continue;

                final Locale locale = Locale.forLanguageTag(localeTag.replace('_', '-'));
                final Configuration config = new Configuration(mapsContext.getResources().getConfiguration());
                config.setLocale(locale);
                final Context localizedContext = mapsContext.createConfigurationContext(config);
                final Resources mapsRes = localizedContext.getResources();

                final int addStopsId = mapsRes.getIdentifier(KEY_ADD_STOPS, "string", MAPS_PACKAGE);
                if (addStopsId != 0) {
                    localizedAddStopsTexts.add(mapsRes.getString(addStopsId));
                }

                final int countStopsId = mapsRes.getIdentifier(KEY_COUNT_STOPS, "plurals", MAPS_PACKAGE);
                if (countStopsId != 0) {
                    final String patternStr = mapsRes.getQuantityString(countStopsId, 5);
                    final String word = patternStr.replace("%d", "").replace("%1$d", "").trim();
                    if (localizedStopsWords.add(word)) {
                        final String regex = patternStr.replace("%d", "(\\d+)").replace("%1$d", "(\\d+)");
                        localizedStopCountPatterns.add(Pattern.compile(regex));
                    }
                }
            }
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Maps package not found", e);
        }

        // Fallbacks
        if (localizedAddStopsTexts.isEmpty()) {
            localizedAddStopsTexts.add("Add stops");
            localizedAddStopsTexts.add("Zwischenstopps hinzufügen");
        }
        if (localizedStopsWords.isEmpty()) {
            localizedStopsWords.add("stops");
            localizedStopsWords.add("Haltestellen");
            localizedStopCountPatterns.add(Pattern.compile("(\\d+)\\s*(stops|Stopps|Haltestellen)"));
        }

        return new MapsContext(localizedAddStopsTexts, localizedStopsWords, localizedStopCountPatterns);
    }
}
