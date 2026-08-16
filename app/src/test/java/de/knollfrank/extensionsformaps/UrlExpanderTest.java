package de.knollfrank.extensionsformaps;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.net.URL;

import de.knollfrank.extensionsformaps.route.url.LongDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.ShortDirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.UrlExpander;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class UrlExpanderTest {

    private MockWebServer mockWebServer;
    private String baseUrl;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // We use a base URL that contains "google" so the predicates in the factories work,
        // but it still points to our local MockWebServer.
        baseUrl = String.format("http://www.google.com.localhost:%d", mockWebServer.getPort());
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testSingleRedirect_returnsTargetUrl() throws IOException {
        // Given
        final URL targetUrl = new URL(baseUrl + "/maps/dir/A/B/");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(301)
                        .setHeader("Location", targetUrl.toString()));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        final URL url = new URL(baseUrl + "/short");
        final ShortDirectionsUrl shortUrl = new ShortDirectionsUrl(url);

        // When
        final LongDirectionsUrl expandedUrl = UrlExpander.expandUrl(shortUrl);

        // Then
        assertEquals(targetUrl, expandedUrl.url());
    }

    @Test
    public void testRedirectToNotFound_returnsTargetUrlAnyway() throws IOException {
        // Given
        final URL targetUrl = new URL(baseUrl + "/maps/dir/A/B/");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(302)
                        .setHeader("Location", targetUrl.toString()));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        
        final URL url = new URL(baseUrl + "/short-to-broken-link");
        final ShortDirectionsUrl shortUrl = new ShortDirectionsUrl(url);

        // When
        final LongDirectionsUrl expandedUrl = UrlExpander.expandUrl(shortUrl);

        // Then
        assertEquals(targetUrl, expandedUrl.url());
    }

    @Test
    public void testExpandUrl_usesHeadMethod() throws IOException, InterruptedException {
        // Given
        final URL targetUrl = new URL(baseUrl + "/maps/dir/A/B/");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(301)
                        .setHeader("Location", targetUrl.toString()));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        
        final URL originalUrl = new URL(baseUrl + "/test");
        final ShortDirectionsUrl shortUrl = new ShortDirectionsUrl(originalUrl);

        // When
        UrlExpander.expandUrl(shortUrl);

        // Then
        final okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("HEAD", request.getMethod());
    }
}
