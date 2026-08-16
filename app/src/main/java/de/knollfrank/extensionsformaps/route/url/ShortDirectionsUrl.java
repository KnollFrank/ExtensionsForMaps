package de.knollfrank.extensionsformaps.route.url;

import java.io.IOException;
import java.net.URL;

public record ShortDirectionsUrl(URL url) implements DirectionsUrl {

    public LongDirectionsUrl expand() throws IOException {
        return UrlExpander.expandUrl(this);
    }
}
