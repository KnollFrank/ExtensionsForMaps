package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

public record OfficialDirectionsUrl(URL url) implements DirectionsUrl {

    @Override
    public List<String> getUrlDecodedAddresses() {
        return OfficialDirectionsUrlAddressesProvider.getUrlDecodedAddresses(this);
    }
}
