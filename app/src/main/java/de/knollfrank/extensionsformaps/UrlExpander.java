package de.knollfrank.extensionsformaps;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import de.knollfrank.extensionsformaps.route.DirectionsUrlPredicate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UrlExpander {

    private static final String TAG = UrlExpander.class.getSimpleName();

    private static final OkHttpClient client =
            new OkHttpClient
                    .Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    // FK-TODO: refactor
    public static URL expandUrl(final URL shortenedUrl) throws IOException {
        for (int attempt = 1; attempt <= 10; attempt++) {
            Log.d(TAG, String.format("Expansion attempt %d for: %s", attempt, shortenedUrl));
            try (final Response response = client.newCall(createRequest(shortenedUrl)).execute()) {
                final URL expandedUrl = response.request().url().url();
                final int code = response.code();
                Log.d(TAG, String.format("Result: %s (Status: %d)", expandedUrl, code));
                // If it's no longer a short URL, we're successful
                if (!DirectionsUrlPredicate.isShortDirectionsUrl(expandedUrl)) {
                    return expandedUrl;
                }
                // Retry conditions:
                // 404: Link not yet active on Google's servers
                // 200: Link active but returning interstitial instead of redirect
                if (code == 404 || code == 200) {
                    Log.d(TAG, String.format("URL not yet ready (Status %d). Retrying in 500ms...", code));
                    sleep(500);
                } else {
                    // Other errors: abort polling
                    return expandedUrl;
                }
            } catch (final IOException e) {
                if (attempt == 10) throw e;
                Log.w(TAG, "Network error during expansion, retrying...", e);
                sleep(500);
            }
        }
        return shortenedUrl;
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Request createRequest(final URL url) {
        String urlString = url.toString();
        if (urlString.startsWith("http://")) {
            urlString = "https://" + urlString.substring(7);
        }
        return new Request.Builder().url(urlString).head().build();
    }
}
