package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

public class GoogleMapsNavigator {

    public static void launchRouteOverview(final Route route, final Context context) {
        launchUrl(RouteToUrlConverter.getUrl(route), context);
    }

    public static void launchUrl(final URL url, final Context context) {
        context.startActivity(createMapIntent(url));
    }

    private static Intent createMapIntent(final URL url) {
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return mapIntent;
    }
}