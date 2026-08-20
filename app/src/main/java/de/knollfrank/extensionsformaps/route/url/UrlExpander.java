package de.knollfrank.extensionsformaps.route.url;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.knollfrank.extensionsformaps.common.Optionals;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// FK-TODO: refactor
class UrlExpander {

    private static final String TAG = UrlExpander.class.getSimpleName();

    private static final OkHttpClient client =
            new OkHttpClient
                    .Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    public static CompletableFuture<DirectionsUrl> expandUrl(final ShortDirectionsUrl shortDirectionsUrl) {
        return CompletableFuture.supplyAsync(() -> _expandUrl(shortDirectionsUrl));
    }

    private static DirectionsUrl _expandUrl(final ShortDirectionsUrl shortDirectionsUrl) {
        URL currentUrl = shortDirectionsUrl.url();
        for (int attempt = 1; attempt <= 10; attempt++) {
            Log.d(TAG, String.format("Expansion attempt %d for: %s", attempt, currentUrl));

            try (final Response response = client.newCall(createRequest(currentUrl)).execute()) {
                final URL resultUrl = response.request().url().url();
                final int code = response.code();

                Log.d(TAG, String.format("Result: %s (Status: %d)", resultUrl, code));

                final Optional<? extends DirectionsUrl> directionsUrl = createDirectionsUrl(resultUrl);
                if (directionsUrl.isPresent()) {
                    return directionsUrl.get();
                }

                // REQUIREMENT 2: Stop the hang in tests.
                // Only retry if it's still a Google short URL type that's not ready.
                if (ShortDirectionsUrlFactory.createShortDirectionsUrl(resultUrl).isPresent() && (code == 404 || code == 200)) {
                    Log.d(TAG, String.format("URL not yet ready (Status %d). Retrying in 500ms...", code));
                    sleep(500);
                    currentUrl = resultUrl;
                } else {
                    throw new RuntimeException(new IOException("Failed to expand short URL to a valid long Directions URL: " + resultUrl));
                }
            } catch (final IOException e) {
                if (attempt == 10) {
                    throw new RuntimeException(e);
                }
                Log.w(TAG, "Network error during expansion, retrying...", e);
                sleep(500);
            }
        }
        throw new RuntimeException(new IOException("Failed to expand short URL after 10 attempts: " + shortDirectionsUrl.url()));
    }

    private static Optional<? extends DirectionsUrl> createDirectionsUrl(final URL url) {
        return Optionals
                .streamOfPresentElements(
                        () -> UnofficialModernDirectionsUrlFactory.createUnofficialModernDirectionsUrl(url),
                        () -> UnofficialLegacyDirectionsUrlFactory.createUnofficialLegacyDirectionsUrl(url))
                .findFirst();
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
