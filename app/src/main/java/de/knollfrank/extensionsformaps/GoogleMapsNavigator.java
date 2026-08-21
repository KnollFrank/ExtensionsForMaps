package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URL;

import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class GoogleMapsNavigator {

    public static void launchRouteOverview(final Route route, final Context context) {
        launchDirectionsUrl(
                RouteDirectionsUrlConverter.getDirectionsUrl(route),
                context);
    }

    public static void launchDirectionsUrl(final DirectionsUrl directionsUrl, final Context context) {
        context.startActivity(createMapIntent(directionsUrl.url()));
    }

    private static Intent createMapIntent(final URL url) {
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return mapIntent;
    }
}