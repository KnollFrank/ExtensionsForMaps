package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.util.List;
import java.util.Optional;

class DirectionsUrl {

    private final URL url;

    public static Optional<DirectionsUrl> of(final URL url) {
        return isDirectionsUrl(url) ?
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

    private static boolean isDirectionsUrl(final URL url) {
        return List.of("http", "https").contains(url.getProtocol()) &&
                url.getHost().contains("google") &&
                url.getPath().startsWith("/maps/dir/");
    }
}
