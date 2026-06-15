package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlExpander {

    public static String expandUrl(final String shortenedUrl) throws IOException {
        String url = shortenedUrl;
        HttpURLConnection conn;

        while (true) {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            final int responseCode = conn.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                final String location = conn.getHeaderField("Location");
                if (location == null) {
                    conn.disconnect();
                    break;
                }
                url = location;
                conn.disconnect();
            } else {
                conn.disconnect();
                break;
            }
        }
        return url;
    }
}
