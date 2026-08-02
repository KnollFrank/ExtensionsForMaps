package de.KnollFrank.routeoptimizerforgooglemaps.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

public class LocalLicenseManager implements LicenseManager {

    private static final String TAG = "LocalLicenseManager";
    private static final String PREFS_NAME = "license_prefs";
    private static final String KEY_IS_PRO = "is_pro";
    private static final String KEY_TEST_KEY = "test_key";

    private final SharedPreferences prefs;
    private final String testKey;

    public LocalLicenseManager(final Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.testKey = getOrCreateTestKey();
        Log.i(TAG, "Device Test License Key: " + testKey);
    }

    @Override
    public boolean isPro() {
        return prefs.getBoolean(KEY_IS_PRO, false);
    }

    @Override
    public boolean isProFeatureRequired(int currentStopCount) {
        // Free version allows up to 15 stops. 
        // If current count is 15, adding another one makes it 16 -> Pro required.
        // If sorting a route with 16 or more stops -> Pro required.
        return currentStopCount > 15;
    }

    @Override
    public boolean activate(String licenseKey) {
        if (testKey.equals(licenseKey)) {
            prefs.edit().putBoolean(KEY_IS_PRO, true).apply();
            return true;
        }
        return false;
    }

    private String getOrCreateTestKey() {
        String key = prefs.getString(KEY_TEST_KEY, null);
        if (key == null) {
            key = "TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            prefs.edit().putString(KEY_TEST_KEY, key).apply();
        }
        return key;
    }
}
