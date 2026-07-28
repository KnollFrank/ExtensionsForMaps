package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Optional;

public class ApiKeyRepository {

    private static final String KEY_ORS_API_KEY = "ors_api_key";

    private ApiKeyRepository() {
    }

    public static void saveApiKey(final Context context, final String apiKey) {
        ApiKeyRepository
                .getSharedPreferences(context)
                .edit()
                .putString(KEY_ORS_API_KEY, apiKey)
                .apply();
    }

    public static Optional<String> getApiKey(final Context context) {
        return Optional.ofNullable(
                ApiKeyRepository
                        .getSharedPreferences(context)
                        .getString(KEY_ORS_API_KEY, null));
    }

    public static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences("route_optimizer_prefs", Context.MODE_PRIVATE);
    }
}
