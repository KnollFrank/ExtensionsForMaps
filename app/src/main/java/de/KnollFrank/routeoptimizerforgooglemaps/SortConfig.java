package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.SharedPreferences;

public class SortConfig {

    private static final String PREFS_NAME = "sort_settings";
    private static final String KEY_SHOW_ROUTE_PREVIEW = "show_route_preview";

    public static boolean shouldShowRoutePreview(final Context context) {
        return getPrefs(context).getBoolean(KEY_SHOW_ROUTE_PREVIEW, true);
    }

    public static void setShouldShowRoutePreview(final Context context, final boolean show) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_ROUTE_PREVIEW, show).apply();
    }

    private static SharedPreferences getPrefs(final Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
