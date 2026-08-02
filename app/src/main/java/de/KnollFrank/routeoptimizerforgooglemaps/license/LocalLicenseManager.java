package de.KnollFrank.routeoptimizerforgooglemaps.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LocalLicenseManager implements LicenseManager {

    private static final String TAG = "LocalLicenseManager";
    private static final String PREFS_NAME = "license_prefs";
    private static final String KEY_IS_PRO = "is_pro";
    private static final String FIXED_LICENSE_KEY = "PRO-VERSION-KEY";

    private final SharedPreferences prefs;

    public LocalLicenseManager(final Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.i(TAG, "Fixed License Key for this version: " + FIXED_LICENSE_KEY);
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
        if (FIXED_LICENSE_KEY.equals(licenseKey)) {
            prefs.edit().putBoolean(KEY_IS_PRO, true).apply();
            return true;
        }
        return false;
    }
}
