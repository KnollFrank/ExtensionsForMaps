package de.knollfrank.extensionsformaps.route.url;

import java.io.IOException;
import java.net.URL;

record ShortDirectionsUrl(URL url) {

    public DirectionsUrl expand() {
        try {
            return UrlExpander.expandUrl(this);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
