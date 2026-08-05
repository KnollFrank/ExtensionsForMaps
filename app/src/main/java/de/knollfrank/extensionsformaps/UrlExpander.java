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

    public static URL expandUrl(final URL urlToExpand) throws IOException {
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

                // Retry conditions for Google short URLs (not yet ready on their side)
                if (code == 404 || code == 200) {
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
