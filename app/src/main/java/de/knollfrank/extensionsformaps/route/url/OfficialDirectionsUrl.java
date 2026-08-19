package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public record OfficialDirectionsUrl(URL url) implements DirectionsUrl {

    // FK-FIXME: implement getUrlDecodedAddresses()
    @Override
    public List<String> getUrlDecodedAddresses() {
        return Collections.emptyList();
    }
}
