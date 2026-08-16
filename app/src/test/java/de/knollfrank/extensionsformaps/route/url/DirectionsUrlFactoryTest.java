package de.knollfrank.extensionsformaps.route.url;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(RobolectricTestRunner.class)
public class DirectionsUrlFactoryTest {

    @Test
    public void testIsDirectionsUrl_withModernUrl() throws MalformedURLException {
        assertTrue(DirectionsUrlFactory
                           .createDirectionsUrl(new URL("https://www.google.com/maps/dir/PointA/PointB/"))
                           .join()
                           .isPresent());
    }

    @Test
    public void testIsDirectionsUrl_withLegacyUrl_returnsTrue() throws MalformedURLException {
        assertTrue(
                DirectionsUrlFactory
                        .createDirectionsUrl(new URL("http://maps.google.com/maps?saddr=A&daddr=B"))
                        .join()
                        .isPresent());
    }

    @Test
    public void testIsShortDirectionsUrl_withLegacyUrl_returnsFalse() throws MalformedURLException {
        assertFalse(
                ShortDirectionsUrlFactory
                        .createShortDirectionsUrl(new URL("http://maps.google.com/maps?saddr=Frauenplan%2021&daddr=discovAIR"))
                        .isPresent());
        assertFalse(
                ShortDirectionsUrlFactory
                        .createShortDirectionsUrl(new URL("https://maps.google.de/maps?saddr=Berlin&daddr=Hamburg"))
                        .isPresent());
    }

    @Test
    public void testIsShortDirectionsUrl_withGooGl_returnsTrue() throws MalformedURLException {
        assertTrue(
                ShortDirectionsUrlFactory
                        .createShortDirectionsUrl(new URL("https://goo.gl/maps/12345"))
                        .isPresent());
    }

    @Test
    public void testIsShortDirectionsUrl_withMapsAppGooGl_returnsTrue() throws MalformedURLException {
        assertTrue(
                ShortDirectionsUrlFactory
                        .createShortDirectionsUrl(new URL("https://maps.app.goo.gl/abcdef"))
                        .isPresent());
    }

    @Test
    public void testIsShortDirectionsUrl_withNonGoogleUrl_returnsFalse() throws MalformedURLException {
        assertFalse(
                ShortDirectionsUrlFactory
                        .createShortDirectionsUrl(new URL("https://example.com/maps?saddr=A"))
                        .isPresent());
    }
}
