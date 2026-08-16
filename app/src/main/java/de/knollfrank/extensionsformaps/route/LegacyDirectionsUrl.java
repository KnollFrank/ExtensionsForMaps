package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.List;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.URLs;

class LegacyDirectionsUrl {

    private final URL url;

    LegacyDirectionsUrl(final URL url) {
        this.url = url;
    }

    public List<String> getUrlDecodedAddresses() {
        return LegacyAddressesProvider.getUrlDecodedAddresses(url);
    }

    public List<String> getGeocodeTokens() {
        return Maps
                .get(URLs.getQuery(url), "geocode")
                .map(LegacyDirectionsUrl::getGeocodeTokens)
                .orElseGet(List::of);
    }

    private static List<String> getGeocodeTokens(final String geocode) {
        return List.of(geocode.split(";"));
    }
}
