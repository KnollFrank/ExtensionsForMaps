package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.List;

class ModernDirectionsUrl {

    private final URL url;

    ModernDirectionsUrl(final URL url) {
        this.url = url;
    }

    public List<String> getUrlDecodedAddresses() {
        return ModernAddressesProvider.getUrlDecodedAddresses(url);
    }

    public List<String> getTokensFromDataPart() {
        return TokenProvider.getTokensFromDataPart(url).orElseThrow();
    }
}
