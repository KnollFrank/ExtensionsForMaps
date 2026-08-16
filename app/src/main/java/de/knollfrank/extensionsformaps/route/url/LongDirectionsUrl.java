package de.knollfrank.extensionsformaps.route.url;

import java.util.List;

public sealed interface LongDirectionsUrl extends DirectionsUrl permits ModernDirectionsUrl, LegacyDirectionsUrl {

    List<String> getUrlDecodedAddresses();
}
