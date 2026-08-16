package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

public record ModernDirectionsUrl(URL url) implements LongDirectionsUrl {

    @Override
    public List<String> getUrlDecodedAddresses() {
        return ModernAddressesProvider.getUrlDecodedAddresses(url);
    }

    public List<String> getTokensFromDataPart() {
        return TokenProvider.getTokensFromDataPart(url).orElseThrow();
    }
}
