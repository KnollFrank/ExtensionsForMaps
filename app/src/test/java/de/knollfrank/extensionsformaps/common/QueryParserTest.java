package de.knollfrank.extensionsformaps.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class QueryParserTest {

    @Test
    public void testParseQuery_withLegacyMapsUrl() throws MalformedURLException {
        // Given
        URL url = new URL("http://maps.google.com/maps?saddr=Frauenplan+21&daddr=discovAIR&dirflg=d");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals(3, params.size());
        assertEquals("Frauenplan 21", params.get("saddr"));
        assertEquals("discovAIR", params.get("daddr"));
        assertEquals("d", params.get("dirflg"));
    }

    @Test
    public void testParseQuery_withGeocodeTokens() throws MalformedURLException {
        // Given
        URL url = new URL("https://maps.google.de/maps?saddr=Berlin&daddr=Hamburg&geocode=G123;G456");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals("G123;G456", params.get("geocode"));
    }

    @Test
    public void testParseQuery_withEncodedSpaces() throws MalformedURLException {
        // Given
        URL url = new URL("https://www.google.com/maps?saddr=San%20Francisco&daddr=Los+Angeles");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals("San Francisco", params.get("saddr"));
        assertEquals("Los Angeles", params.get("daddr"));
    }

    @Test
    public void testParseQuery_noQueryParameters() throws MalformedURLException {
        // Given
        URL url = new URL("https://www.google.com/maps/dir/PointA/PointB/");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertTrue(params.isEmpty());
    }

    @Test
    public void testParseQuery_emptyValues() throws MalformedURLException {
        // Given
        URL url = new URL("http://example.com/search?q=&lang=de");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals("", params.get("q"));
        assertEquals("de", params.get("lang"));
    }

    @Test
    public void testParseQuery_multipleSameKeys_returnsFirstOne() throws MalformedURLException {
        // Given: android.net.Uri returns the first value for getQueryParameter if multiple exist
        URL url = new URL("http://example.com/?a=1&a=2");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals("1", params.get("a"));
    }

    @Test
    public void testParseQuery_withSpecialCharacters() throws MalformedURLException {
        // Given
        URL url = new URL("https://www.google.com/maps?g_st=ac&utm_source=test.source");

        // When
        Map<String, String> params = QueryParser.parseQuery(url);

        // Then
        assertEquals("ac", params.get("g_st"));
        assertEquals("test.source", params.get("utm_source"));
    }
}
