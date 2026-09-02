package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import de.knollfrank.extensionsformaps.common.ResourcesWrapper;

public class GoogleAppContextResolver {

    public static GoogleAppContext resolve(final Context context) {
        try {
            return _resolve(getGoogleAppContext(context));
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Google App package not found", e);
        }
    }

    private static Context getGoogleAppContext(final Context context) throws PackageManager.NameNotFoundException {
        return context.createPackageContext(GOOGLE_APP_PACKAGE, 0);
    }

    private static GoogleAppContext _resolve(final Context context) {
        final Resources resources = context.getResources();
        return new GoogleAppContext(
                getString(resources, "searchbox_aim_ask_anything_text"),
                getString(resources, "searchbox_aim_send_button_content_description"),
                getString(resources, "googleapp_sbn_aim_chip_display_label"),
                getString(resources, "searchbox_aim_photo_button_content_description"));
    }

    private static String getString(final Resources resources, final String resourceName) {
        return resources.getString(
                new ResourcesWrapper(resources)
                        .getValidIdentifierOrElseThrow(
                                resourceName,
                                "string",
                                GOOGLE_APP_PACKAGE));
    }
}
