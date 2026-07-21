package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UrlExpander {

    private static final OkHttpClient client =
            new OkHttpClient
                    .Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build();

    public static URL expandUrl(final URL shortenedUrl) throws IOException {
        try (final Response response = client.newCall(createRequest(shortenedUrl)).execute()) {
            return response.request().url().url();
        }
    }

    private static Request createRequest(final URL url) {
        return new Request.Builder().url(url).head().build();
    }
}