package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.Intent;

import java.net.URL;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;

class ExtraMapsUrlSenderAndReceiver {

    private static final String EXTRA_MAPS_URL = "EXTRA_MAPS_URL";

    public static void sendExtraMapsUrl(final URL url, final Context context) {
        context.startActivity(createExtraMapsUrlIntent(url, context));
    }

    // FK-TODO: refactor
    public static Optional<URL> receiveExtraMapsUrl(final Optional<Intent> intent) {
        if (intent.isPresent() && intent.orElseThrow().hasExtra(EXTRA_MAPS_URL)) {
            final String url = intent.orElseThrow().getStringExtra(EXTRA_MAPS_URL);
            return Optional.of(URLs.createUrl(url));
        }
        return Optional.empty();
    }

    private static Intent createExtraMapsUrlIntent(final URL url, final Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MAPS_URL, url.toString());
        return intent;
    }
}
