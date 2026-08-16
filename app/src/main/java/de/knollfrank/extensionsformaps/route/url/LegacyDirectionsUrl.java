package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.URLs;

public record LegacyDirectionsUrl(URL url) implements DirectionsUrl {

    @Override
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
