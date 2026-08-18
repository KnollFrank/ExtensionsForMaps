package de.knollfrank.extensionsformaps.route.url;

import java.net.URL;
import java.util.List;

public sealed interface DirectionsUrl permits UnofficialModernDirectionsUrl, UnofficialLegacyDirectionsUrl {

    URL url();

    List<String> getUrlDecodedAddresses();
}
