package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;

public class GoogleMapsNavigator {

    public static void launchRouteOverview(final Route route, final Context context) {
        if (route.stops().size() < 2) {
            // FK-TODO: anders behandeln, nicht einfach stillschweigend mit "return" nichts tun
            return;
        }
        launchUrl(RouteToUrlConverter.getUrl(route), context);
    }

    private static void launchUrl(final String url, final Context context) {
        final Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mapIntent);
    }
}