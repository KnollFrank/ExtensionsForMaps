package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.SharedPreferences;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OptimizationType;

public class SortConfig {

    public enum OptimizationMethod {
        HAVERSINE,
        OPEN_ROUTE_SERVICE
    }

    private static final String PREFS_NAME = "sort_settings";
    private static final String KEY_SHOW_ROUTE_PREVIEW = "show_route_preview";
    private static final String KEY_OPTIMIZATION_METHOD = "optimization_method";
    private static final String KEY_OPTIMIZATION_TYPE = "optimization_type";

    public static boolean shouldShowRoutePreview(final Context context) {
        if (!BuildConfig.FEATURE_ROUTE_PREVIEW_VISIBLE) {
            return false;
        }
        return getPrefs(context).getBoolean(KEY_SHOW_ROUTE_PREVIEW, true);
    }

    public static void setShouldShowRoutePreview(final Context context, final boolean show) {
        SortConfig
                .getPrefs(context)
                .edit()
                .putBoolean(KEY_SHOW_ROUTE_PREVIEW, show)
                .apply();
    }

    public static OptimizationMethod getOptimizationMethod(final Context context) {
        final String method = getPrefs(context).getString(KEY_OPTIMIZATION_METHOD, OptimizationMethod.HAVERSINE.name());
        try {
            return OptimizationMethod.valueOf(method);
        } catch (final IllegalArgumentException e) {
            return OptimizationMethod.HAVERSINE;
        }
    }

    public static void setOptimizationMethod(final Context context, final OptimizationMethod method) {
        SortConfig
                .getPrefs(context)
                .edit()
                .putString(KEY_OPTIMIZATION_METHOD, method.name())
                .apply();
    }

    public static OptimizationType getOptimizationType(final Context context) {
        final String type = getPrefs(context).getString(KEY_OPTIMIZATION_TYPE, OptimizationType.FIXED_DESTINATION.name());
        try {
            return OptimizationType.valueOf(type);
        } catch (final IllegalArgumentException e) {
            return OptimizationType.FIXED_DESTINATION;
        }
    }

    public static void setOptimizationType(final Context context, final OptimizationType type) {
        SortConfig
                .getPrefs(context)
                .edit()
                .putString(KEY_OPTIMIZATION_TYPE, type.name())
                .apply();
    }

    private static SharedPreferences getPrefs(final Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
