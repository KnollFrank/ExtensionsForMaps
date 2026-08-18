package de.knollfrank.extensionsformaps.route.url;

import java.util.List;

import de.knollfrank.extensionsformaps.common.Maps;
import de.knollfrank.extensionsformaps.common.URLs;

class GeocodeTokensProvider {

    public static List<String> getGeocodeTokens(final UnofficialLegacyDirectionsUrl unofficialLegacyDirectionsUrl) {
        return Maps
                .get(URLs.getQuery(unofficialLegacyDirectionsUrl.url()), "geocode")
                .map(GeocodeTokensProvider::getGeocodeTokens)
                .orElseGet(List::of);
    }

    private static List<String> getGeocodeTokens(final String geocode) {
        return List.of(geocode.split(";"));
    }
}
