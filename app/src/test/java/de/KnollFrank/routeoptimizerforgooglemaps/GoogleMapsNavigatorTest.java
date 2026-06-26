package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application; // <-- Neuer Import
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows; // <-- Wichtiger neuer Import
import org.robolectric.shadows.ShadowApplication;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class GoogleMapsNavigatorTest {

    @Test
    public void testLaunchRouteOverview_buildsCorrectIntentAndUrl() {
        // Arrange
        final Context context = ApplicationProvider.getApplicationContext();
        final List<RouteOptimizer.Stop> optimizedStops =
                Arrays.asList(
                        new RouteOptimizer.Stop("Start", 48.5216, 9.0576),      // Start
                        new RouteOptimizer.Stop("Waypoint1", 47.3769, 8.5417),  // Zwischenstopp
                        new RouteOptimizer.Stop("Destination", 45.4642, 9.1900) // Ziel
                             );

        // Act
        GoogleMapsNavigator.launchRouteOverview(context, optimizedStops);

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
                        "&origin=48.5216,9.0576" +
                        "&destination=45.4642,9.19" +
                        "&waypoints=47.3769,8.5417";
        assertEquals(expectedUrl, nextStartedActivity.getData().toString());
    }
}