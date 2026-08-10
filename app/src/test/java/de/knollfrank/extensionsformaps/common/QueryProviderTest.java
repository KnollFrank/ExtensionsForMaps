package de.knollfrank.extensionsformaps.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(RobolectricTestRunner.class)
public class QueryProviderTest {

    @Test
    public void testGetQuery_withLegacyMapsUrl() throws MalformedURLException {
        // Given
        final URL url = new URL("http://maps.google.com/maps?saddr=Frauenplan+21&daddr=discovAIR&dirflg=d");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals(
                ImmutableMap
                        .<String, String>builder()
                        .put("saddr", "Frauenplan 21")
                        .put("daddr", "discovAIR")
                        .put("dirflg", "d")
                        .build(),
                params);
    }

    @Test
    public void testGetQuery_withGeocodeTokens() throws MalformedURLException {
        // Given
        final URL url = new URL("https://maps.google.de/maps?saddr=Berlin&daddr=Hamburg&geocode=G123;G456");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals("G123;G456", params.get("geocode"));
    }

    @Test
    public void testGetQuery_withEncodedSpaces() throws MalformedURLException {
        // Given
        final URL url = new URL("https://www.google.com/maps?saddr=San%20Francisco&daddr=Los+Angeles");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals("San Francisco", params.get("saddr"));
        assertEquals("Los Angeles", params.get("daddr"));
    }

    @Test
    public void testGetQuery_noQueryParameters() throws MalformedURLException {
        // Given
        final URL url = new URL("https://www.google.com/maps/dir/PointA/PointB/");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertTrue(params.isEmpty());
    }

    @Test
    public void testGetQuery_emptyValue() throws MalformedURLException {
        // Given
        final URL url = new URL("http://example.com/search?q=&lang=de");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals("", params.get("q"));
        assertEquals("de", params.get("lang"));
    }

    @Test
    public void testGetQuery_multipleSameKeys_returnsFirstOne() throws MalformedURLException {
        // Given: android.net.Uri returns the first value for getQueryParameter if multiple exist
        URL url = new URL("http://example.com/?a=1&a=2");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals("1", params.get("a"));
    }

    @Test
    public void testGetQuery_withSpecialCharacters() throws MalformedURLException {
        // Given
        final URL url = new URL("https://www.google.com/maps?g_st=ac&utm_source=test.source");

        // When
        final ImmutableMap<String, String> params = QueryProvider.getQuery(url);

        // Then
        assertEquals("ac", params.get("g_st"));
        assertEquals("test.source", params.get("utm_source"));
    }
}
