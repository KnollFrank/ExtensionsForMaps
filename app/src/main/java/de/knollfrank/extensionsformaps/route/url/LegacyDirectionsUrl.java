package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

public record LegacyDirectionsUrl(URL url) implements DirectionsUrl {

    @Override
    public List<String> getUrlDecodedAddresses() {
        return LegacyAddressesProvider.getUrlDecodedAddresses(this);
    }

    public List<String> getGeocodeTokens() {
        return GeocodeTokensProvider.getGeocodeTokens(this);
    }
}
