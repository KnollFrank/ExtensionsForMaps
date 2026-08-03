package de.knollfrank.extensionsformaps;

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
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.coordinate.Unit;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

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
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.5216, Unit.DEGREES),
                                        new Angle(9.0576, Unit.DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "1",
                                        "Waypoint1",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(47.3769, Unit.DEGREES),
                                                new Angle(8.5417, Unit.DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "2",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(45.4642, Unit.DEGREES),
                                        new Angle(9.1900, Unit.DEGREES)),
                                Optional.empty()));

        // Act
        GoogleMapsNavigator.launchRouteOverview(route, ApplicationProvider.getApplicationContext());

        // Assert
        // NEU: So holst du dir den gefeuerten Intent in aktuellen Robolectric-Versionen:
        final ShadowApplication shadowApp = Shadows.shadowOf(ApplicationProvider.<Application>getApplicationContext());
        final Intent nextStartedActivity = shadowApp.getNextStartedActivity();

        assertNotNull("Es wurde kein Intent gestartet", nextStartedActivity);

        assertEquals(Intent.ACTION_VIEW, nextStartedActivity.getAction());
        assertEquals("com.google.android.apps.maps", nextStartedActivity.getPackage());

        // Prüfen, ob die URL exakt nach der offiziellen Maps Directions API gebaut wurde
        final String expectedUrl =
                "https://www.google.com/maps/dir/?api=1" +
                        "&origin=48.5216%2C9.0576" +
                        "&destination=45.4642%2C9.19" +
                        "&waypoints=47.3769%2C8.5417";
        assertEquals(expectedUrl, nextStartedActivity.getData().toString());
    }
}