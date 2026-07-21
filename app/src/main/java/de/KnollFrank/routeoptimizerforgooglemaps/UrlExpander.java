package de.KnollFrank.routeoptimizerforgooglemaps;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UrlExpander {

    private static final String TAG = "UrlExpander";

    private static final OkHttpClient client =
            new OkHttpClient
                    .Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    public static URL expandUrl(final URL shortenedUrl) throws IOException {
        Log.d(TAG, "Expanding URL: " + shortenedUrl);
        try (final Response response = client.newCall(createRequest(shortenedUrl)).execute()) {
            final URL expandedUrl = response.request().url().url();
            Log.d(TAG, String.format("Expanded URL: %s (Status: %d)", expandedUrl, response.code()));
            return expandedUrl;
        }
    }

    private static Request createRequest(final URL url) {
        return new Request
                .Builder()
                .url(url)
                .head()
                .build();
    }
}
