package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

record ShortDirectionsUrl(URL url) {

    public CompletableFuture<DirectionsUrl> expand() {
        return UrlExpander.expandUrl(this);
    }
}
