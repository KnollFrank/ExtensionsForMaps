package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class GoogleMapsNavigatorTest {

    @Test
    public void testLaunchRouteOverview_buildsCorrectIntentAndUrl() {
        // Arrange
        final Route route =
                new Route(
                        new Stop(
                                "0",
                                "Start",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.5216, Unit.DEGREES),
                                        new Angle(9.0576, Unit.DEGREES))),
                        List.of(
                                new Stop(
                                        "1",
                                        "Waypoint1",
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(47.3769, Unit.DEGREES),
                                                new Angle(8.5417, Unit.DEGREES)))),
                        new Stop(
                                "2",
                                "Destination",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(45.4642, Unit.DEGREES),
                                        new Angle(9.1900, Unit.DEGREES))));

        // Act
        GoogleMapsNavigator.launchRouteOverview(route, ApplicationProvider.getApplicationContext());

        // Assert
        // NEU: So holst du dir den gefeuerten Intent in aktuellen Robolectric-Versionen:
        final ShadowApplication shadowApp = Shadows.shadowOf(ApplicationProvider.<Application>getApplicationContext());
        final Intent nextStartedActivity = shadowApp.getNextStartedActivity();

        assertNotNull("Es wurde kein Intent gestartet", nextStartedActivity);

        assertEquals(Intent.ACTION_VIEW, nextStartedActivity.getAction());
        assertEquals("com.google.android.apps.maps", nextStartedActivity.getPackage());

        final String expectedUrl = "https://www.google.com/maps/dir/Start/Waypoint1/Destination/data=!3m2!1e3!4b1!4m13!4m12!1m3!2m2!1d9.0576!2d48.5216!1m3!2m2!1d8.5417!2d47.3769!1m3!2m2!1d9.19!2d45.4642?entry=ttu";
        assertEquals(expectedUrl, nextStartedActivity.getData().toString());
    }
}