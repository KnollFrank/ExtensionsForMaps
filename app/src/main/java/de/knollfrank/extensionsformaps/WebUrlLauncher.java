package de.knollfrank.extensionsformaps;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Optional;

public class WebUrlLauncher {

    public static void openUrl(final Context context, final String url) {
        if (url.trim().isEmpty()) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            handleNoBrowserFound(context, url);
        }
    }

    private static void handleNoBrowserFound(final Context context, final String url) {
        Toast
                .makeText(context, R.string.error_no_browser, Toast.LENGTH_LONG)
                .show();
        copyToClipboard(context, url);
    }

    private static void copyToClipboard(final Context context, final String url) {
        Optional
                .ofNullable((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .ifPresent(
                        clipboardManager -> {
                            final ClipData clip = ClipData.newPlainText("URL", url);
                            clipboardManager.setPrimaryClip(clip);
                            Toast
                                    .makeText(context, R.string.url_copied_to_clipboard, Toast.LENGTH_SHORT)
                                    .show();
                        });
    }
}
