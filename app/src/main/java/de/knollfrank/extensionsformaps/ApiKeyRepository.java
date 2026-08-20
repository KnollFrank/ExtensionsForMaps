package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Optional;

public class ApiKeyRepository {

    private static final String KEY_ORS_API_KEY = "ors_api_key";
    private final Context context;

    public ApiKeyRepository(final Context context) {
        this.context = context;
    }

    public void saveApiKey(final String apiKey) {
        this
                .getSharedPreferences()
                .edit()
                .putString(KEY_ORS_API_KEY, apiKey)
                .apply();
    }

    public Optional<String> getApiKey() {
        return Optional.ofNullable(
                this
                        .getSharedPreferences()
                        .getString(KEY_ORS_API_KEY, null));
    }

    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("route_optimizer_prefs", Context.MODE_PRIVATE);
    }
}
