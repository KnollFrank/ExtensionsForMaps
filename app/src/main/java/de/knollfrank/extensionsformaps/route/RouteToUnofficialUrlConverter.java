package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.knollfrank.extensionsformaps.common.URLs;

class RouteToUnofficialUrlConverter {

    public static URL getUnofficialUrl(final Route route) {
        final StringBuilder pathBuilder = new StringBuilder("https://www.google.com/maps/dir");
        final List<String> dataTokens = new ArrayList<>();

        for (final Stop stop : route.stops()) {
            pathBuilder.append("/").append(Uri.encode(stop.address()));
            // FK-TODO: use ifPresentOrElse()
            if (stop.officialPlaceId().isPresent()) {
                dataTokens.add("1m5");
                dataTokens.add("1m4");
                dataTokens.add("1s" + stop.officialPlaceId().get().toUndocumentedPlaceId().value());
                dataTokens.add("8m2");
                dataTokens.add("3d" + RouteToOfficialUrlConverter.format(stop.geodetic().getLatitude()));
                dataTokens.add("4d" + RouteToOfficialUrlConverter.format(stop.geodetic().getLongitude()));
            } else {
                dataTokens.add("1m3");
                dataTokens.add("2m2");
                dataTokens.add("1d" + RouteToOfficialUrlConverter.format(stop.geodetic().getLongitude()));
                dataTokens.add("2d" + RouteToOfficialUrlConverter.format(stop.geodetic().getLatitude()));
            }
        }

        final int innerCount = dataTokens.size();
        final int outerCount = innerCount + 1;

        final StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("!3m2!1e3!4b1");
        dataBuilder.append("!4m").append(outerCount);
        dataBuilder.append("!4m").append(innerCount);
        for (final String token : dataTokens) {
            dataBuilder.append("!").append(token);
        }

        pathBuilder.append("/data=").append(dataBuilder).append("?entry=ttu");
        return URLs.createUrl(pathBuilder.toString());
    }
}
