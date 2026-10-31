package de.knollfrank.extensionsformaps;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

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
        try {
            context.startActivity(createMapIntent(directionsUrl.url()));
        } catch (final ActivityNotFoundException e) {
            Toast
                    .makeText(context, R.string.error_google_maps_not_installed, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private static Intent createMapIntent(final URL url) {
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        mapIntent.setPackage(PackageNames.GOOGLE_MAPS_PACKAGE);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return mapIntent;
    }
}