package de.knollfrank.extensionsformaps.route;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.URLs;

class DirectionsUrl {

    private final URL url;

    public static Optional<DirectionsUrl> of(final URL url) {
        return DirectionsUrlPredicate.isDirectionsUrl(url) ?
                Optional.of(new DirectionsUrl(url)) :
                Optional.empty();
    }

    private DirectionsUrl(final URL url) {
        this.url = url;
    }

    public List<String> getUrlDecodedAddresses() {
        return AddressesProvider.getUrlDecodedAddresses(url);
    }

    public Optional<List<String>> getTokensFromDataPart() {
        if (DirectionsUrlPredicate.isModernDirectionsUrl(url)) {
            return TokenProvider.getTokensFromDataPart(url);
        }
        return Optional.empty();
    }

    public Optional<List<String>> getGeocodeTokens() {
        final String query = url.getQuery();
        if (query == null) {
            return Optional.empty();
        }
        final String geocode = URLs.parseQuery(query).get("geocode");
        return geocode != null ?
                Optional.of(List.of(URLs.decode(geocode).split(";"))) :
                Optional.empty();
    }
}
