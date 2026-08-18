package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

public record UnofficialModernDirectionsUrl(URL url) implements DirectionsUrl {

    @Override
    public List<String> getUrlDecodedAddresses() {
        return ModernAddressesProvider.getUrlDecodedAddresses(this);
    }

    public List<String> getTokensFromDataPart() {
        return TokenProvider.getTokensFromDataPart(this);
    }
}
