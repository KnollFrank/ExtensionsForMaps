package de.knollfrank.extensionsformaps.route.extract;

import androidx.core.util.Pair;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Lists;
import de.knollfrank.extensionsformaps.route.OfficialPlaceId;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteFactory;
import de.knollfrank.extensionsformaps.route.url.UnofficialLegacyDirectionsUrl;

class LegacyDirectionsUrlRouteExtractor {

    public static Route extractRoute(final UnofficialLegacyDirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        extractDataFromGeocodeTokensIntoStopDataList(directionsUrl.getGeocodeTokens(), stopDataList);
        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    private static void extractDataFromGeocodeTokensIntoStopDataList(final List<String> geocodeTokens, final List<StopData> stopDataList) {
        LegacyDirectionsUrlRouteExtractor
                .zipToShortest(geocodeTokens, stopDataList)
                .forEach(token_stopData -> extractDataFromGeocodeTokenIntoStopData(token_stopData.first, token_stopData.second));
    }

    private static void extractDataFromGeocodeTokenIntoStopData(final String geocodeToken, final StopData stopData) {
        GeocodeTokenParser
                .parseToken(geocodeToken)
                .ifPresent(geocodeData -> {
                    geocodeData.latitude.ifPresent(lat -> stopData.latitude = Optional.of(lat));
                    geocodeData.longitude.ifPresent(lon -> stopData.longitude = Optional.of(lon));
                    geocodeData.officialPlaceId.ifPresent(fid -> stopData.officialPlaceId = Optional.of(new OfficialPlaceId(fid)));
                });
    }

    private static <A, B> List<Pair<A, B>> zipToShortest(final List<A> as, final List<B> bs) {
        final Pair<List<A>, List<B>> truncated = Lists.truncateToCommonSize(as, bs);
        return Lists.zip(truncated.first, truncated.second);
    }
}
