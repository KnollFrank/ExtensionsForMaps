package de.knollfrank.extensionsformaps.route.extract;

import android.net.Uri;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.route.OfficialPlaceId;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteFactory;
import de.knollfrank.extensionsformaps.route.url.OfficialDirectionsUrl;

class OfficialDirectionsUrlRouteExtractor {

    public static Route extractRoute(final OfficialDirectionsUrl directionsUrl) {
        final List<StopData> stopDataList = AddressToStopDataConverter.convert(directionsUrl.getUrlDecodedAddresses());
        final Uri uri = URLs.createUri(directionsUrl.url());

        // origin -> index 0
        // destination -> index size-1
        // waypoints -> indices 1 to size-2

        if (stopDataList.isEmpty()) {
            throw new IllegalArgumentException("No stops found in official directions URL");
        }

        assignPlaceId(uri, "origin_place_id", stopDataList.get(0));

        if (stopDataList.size() > 1) {
            assignPlaceId(uri, "destination_place_id", stopDataList.get(stopDataList.size() - 1));

            final String waypointsPidStr = uri.getQueryParameter("waypoint_place_ids");
            if (waypointsPidStr != null && !waypointsPidStr.isEmpty()) {
                final String[] pids = waypointsPidStr.split("\\|", -1);
                for (int i = 0; i < pids.length && (i + 1) < stopDataList.size() - 1; i++) {
                    final String pid = pids[i];
                    if (!pid.isEmpty()) {
                        stopDataList.get(i + 1).officialPlaceId = Optional.of(new OfficialPlaceId(pid));
                    }
                }
            }
        }

        return RouteFactory.createRoute(StopDataConverter.asStops(stopDataList));
    }

    private static void assignPlaceId(final Uri uri, final String paramName, final StopData stopData) {
        final String pid = uri.getQueryParameter(paramName);
        if (pid != null && !pid.isEmpty()) {
            stopData.officialPlaceId = Optional.of(new OfficialPlaceId(pid));
        }
    }
}
