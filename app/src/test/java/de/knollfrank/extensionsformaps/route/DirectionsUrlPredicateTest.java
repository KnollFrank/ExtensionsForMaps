package de.knollfrank.extensionsformaps.route;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(RobolectricTestRunner.class)
public class DirectionsUrlPredicateTest {

    @Test
    public void testIsDirectionsUrl_withModernUrl() throws MalformedURLException {
        assertTrue(DirectionsUrlPredicate.isDirectionsUrl(new URL("https://www.google.com/maps/dir/PointA/PointB/")));
    }

    @Test
    public void testIsDirectionsUrl_withLegacyUrl_returnsTrue() throws MalformedURLException {
        assertTrue(DirectionsUrlPredicate.isDirectionsUrl(new URL("http://maps.google.com/maps?saddr=A&daddr=B")));
    }

    @Test
    public void testIsShortDirectionsUrl_withLegacyUrl_returnsFalse() throws MalformedURLException {
        assertFalse(DirectionsUrlPredicate.isShortDirectionsUrl(new URL("http://maps.google.com/maps?saddr=Frauenplan%2021&daddr=discovAIR")));
        assertFalse(DirectionsUrlPredicate.isShortDirectionsUrl(new URL("https://maps.google.de/maps?saddr=Berlin&daddr=Hamburg")));
    }

    @Test
    public void testIsShortDirectionsUrl_withGooGl_returnsTrue() throws MalformedURLException {
        assertTrue(DirectionsUrlPredicate.isShortDirectionsUrl(new URL("https://goo.gl/maps/12345")));
    }

    @Test
    public void testIsShortDirectionsUrl_withMapsAppGooGl_returnsTrue() throws MalformedURLException {
        assertTrue(DirectionsUrlPredicate.isShortDirectionsUrl(new URL("https://maps.app.goo.gl/abcdef")));
    }

    @Test
    public void testIsShortDirectionsUrl_withNonGoogleUrl_returnsFalse() throws MalformedURLException {
        assertFalse(DirectionsUrlPredicate.isShortDirectionsUrl(new URL("https://example.com/maps?saddr=A")));
    }
}
