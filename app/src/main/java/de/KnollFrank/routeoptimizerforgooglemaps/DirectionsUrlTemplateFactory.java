package de.KnollFrank.routeoptimizerforgooglemaps;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;

public class DirectionsUrlTemplateFactory {

    // FK-TODO: rufe intern den RouteToUrlConverter nachdem eine neue Methode geschrieben wurde, die eine Route mit totalStops erzeugt analog zum aktuellen Code in createDirectionsUrlTemplate()
    public static URL createDirectionsUrlTemplate(final Geodetic base, int totalStops) throws MalformedURLException {
        if (totalStops < 1) {
            throw new IllegalArgumentException("totalStops: " + totalStops);
        }

        final StringBuilder pathBuilder = new StringBuilder("https://www.google.com/maps/dir");
        final StringBuilder dataBuilder = new StringBuilder();

        int tokenCountInner = 4 * totalStops;
        int tokenCountOuter = tokenCountInner + 1;

        // 1. Google Protobuf-Header berechnen
        dataBuilder
                .append("!3m2!1e3!4b1")
                .append("!4m").append(tokenCountOuter)
                .append("!4m").append(tokenCountInner);

        // 2. Ersten Punkt anlegen (Die echte Apotheke)
        pathBuilder.append("/").append(Uri.encode("Wegpunkt 1"));
        dataBuilder.append(
                String.format(
                        Locale.US,
                        "!1m3!2m2!1d%.4f!2d%.4f",
                        base.getLongitude().toDegrees(),
                        base.getLatitude().toDegrees()));

        // 3. Die restlichen Dummy-Slots per Micro-Shifting generieren
        final Geodetic shift =
                Geodetic.fromLatitudeLongitude(
                        new Angle(0.0005, Unit.DEGREES),
                        new Angle(0.0003, Unit.DEGREES));
        for (int i = 1; i < totalStops; i++) {
            // Text für das UI-Feld erzeugen
            final String label = String.format(Locale.US, "Wegpunkt %d", i + 1);
            pathBuilder.append("/").append(Uri.encode(label));

            // Koordinaten leicht verschieben (ca. 50 Meter pro Schritt)
            final Geodetic shifted = base.add(shift.mul(i));
            dataBuilder.append(String.format(Locale.US, "!1m3!2m2!1d%.4f!2d%.4f", shifted.getLongitude().toDegrees(), shifted.getLatitude().toDegrees()));
        }

        // 4. Alles zusammenfügen
        pathBuilder
                .append("/data=")
                .append(dataBuilder)
                .append("?entry=ttu");

        return new URL(pathBuilder.toString());
    }
}
