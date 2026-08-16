package de.knollfrank.extensionsformaps;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import de.knollfrank.extensionsformaps.route.DirectionsUrlPredicate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// FK-TODO: refactor
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

    public static URL expandUrl(final URL urlToExpand) throws IOException {
        // REQUIREMENT 1: If it's already a full Google Maps URL (Legacy or Modern),
        // return it immediately. No network calls for legacy URLs.
        if (DirectionsUrlPredicate.isDirectionsUrl(urlToExpand)) {
            return urlToExpand;
        }

        URL currentUrl = urlToExpand;
        for (int attempt = 1; attempt <= 10; attempt++) {
            Log.d(TAG, String.format("Expansion attempt %d for: %s", attempt, currentUrl));

            try (final Response response = client.newCall(createRequest(currentUrl)).execute()) {
                final URL resultUrl = response.request().url().url();
                final int code = response.code();

                Log.d(TAG, String.format("Result: %s (Status: %d)", resultUrl, code));

                // If it's no longer a short URL, we are successful
                if (!DirectionsUrlPredicate.isShortDirectionsUrl(resultUrl)) {
                    return resultUrl;
                }

                // REQUIREMENT 2: Stop the hang in tests.
                // Only retry if it's still a Google short URL type that's not ready.
                if (DirectionsUrlPredicate.isShortDirectionsUrl(resultUrl) && (code == 404 || code == 200)) {
                    Log.d(TAG, String.format("URL not yet ready (Status %d). Retrying in 500ms...", code));
                    sleep(500);
                    currentUrl = resultUrl;
                } else {
                    return resultUrl;
                }
            } catch (final IOException e) {
                if (attempt == 10) throw e;
                Log.w(TAG, "Network error during expansion, retrying...", e);
                sleep(500);
            }
        }
        return currentUrl;
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Request createRequest(final URL url) {
        return new Request.Builder().url(url).head().build();
    }
}
