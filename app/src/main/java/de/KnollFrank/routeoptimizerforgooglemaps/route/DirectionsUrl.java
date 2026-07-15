package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.List;
import java.util.Optional;

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
        return TokenProvider.getTokensFromDataPart(url);
    }
}
