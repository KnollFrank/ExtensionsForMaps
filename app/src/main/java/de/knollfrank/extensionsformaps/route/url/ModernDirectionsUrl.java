package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

import de.knollfrank.extensionsformaps.route.TokenProvider;

public class ModernDirectionsUrl {

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
