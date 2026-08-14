package de.knollfrank.extensionsformaps.license;

import android.content.Context;

class GumroadLicenseManagerFactory {

    public static GumroadLicenseManager createGumroadLicenseManager(final Context context) {
        return new GumroadLicenseManager(
                GumroadServiceFactory.createGumroadService(),
                context.getSharedPreferences("license_prefs", Context.MODE_PRIVATE));
    }
}
