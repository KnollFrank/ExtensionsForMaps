package de.knollfrank.extensionsformaps.accessibility;

import static de.knollfrank.extensionsformaps.accessibility.PackageNames.GOOGLE_APP_PACKAGE;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Optionals;
import de.knollfrank.extensionsformaps.common.ResourcesWrapper;

public class GoogleAppContextResolver {

    public static Optional<GoogleAppContext> resolve(final Context context) {
        return GoogleAppContextResolver
                .getGoogleAppContext(context)
                .flatMap(GoogleAppContextResolver::_resolve);
    }

    private static Optional<Context> getGoogleAppContext(final Context context) {
        try {
            return Optional.of(context.createPackageContext(GOOGLE_APP_PACKAGE, 0));
        } catch (final NameNotFoundException e) {
            return Optional.empty();
        }
    }

    private static Optional<GoogleAppContext> _resolve(final Context context) {
        final Resources resources = context.getResources();
        final Optional<String> askAnythingText = getString(resources, "searchbox_aim_ask_anything_text");
        final Optional<String> sendText = getString(resources, "searchbox_aim_send_button_content_description");
        final Optional<String> aiModeText = getString(resources, "googleapp_sbn_aim_chip_display_label");
        final Optional<String> takePhotoText = getString(resources, "searchbox_aim_photo_button_content_description");
        return askAnythingText.isPresent() && sendText.isPresent() && aiModeText.isPresent() && takePhotoText.isPresent() ?
                Optional.of(
                        new GoogleAppContext(
                                askAnythingText.get(),
                                sendText.get(),
                                aiModeText.get(),
                                takePhotoText.get())) :
                Optional.empty();
    }

    private static Optional<String> getString(final Resources resources, final String resourceName) {
        return Optionals
                .asOptional(
                        new ResourcesWrapper(resources)
                                .getIdentifier(
                                        resourceName,
                                        "string",
                                        GOOGLE_APP_PACKAGE))
                .map(resources::getString);
    }
}
