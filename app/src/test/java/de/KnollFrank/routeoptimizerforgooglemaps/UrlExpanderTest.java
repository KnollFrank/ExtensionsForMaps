package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.net.URL;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class UrlExpanderTest {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testNoRedirect_returnsOriginalUrl() throws IOException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        final URL originalUrl =
                mockWebServer
                        .url("/no-redirect")
                        .url();

        // When
        final URL expandedUrl = UrlExpander.expandUrl(originalUrl);

        // Then
        assertEquals(originalUrl, expandedUrl);
    }

    @Test
    public void testSingleRedirect_returnsTargetUrl() throws IOException {
        // Given
        final URL targetUrl = mockWebServer.url("/final-destination").url();
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(301)
                        .setHeader("Location", targetUrl));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        final URL shortUrl = mockWebServer.url("/short").url();

        // When
        final URL expandedUrl = UrlExpander.expandUrl(shortUrl);

        // Then
        assertEquals(targetUrl, expandedUrl);
    }

    @Test
    public void testRedirectToNotFound_returnsTargetUrlAnyway() throws IOException {
        // Given
        final URL targetUrl = mockWebServer.url("/404-page").url();
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(302)
                        .setHeader("Location", targetUrl));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        final URL shortUrl = mockWebServer.url("/short-to-broken-link").url();

        // When
        final URL expandedUrl = UrlExpander.expandUrl(shortUrl);

        // Then
        assertEquals(targetUrl, expandedUrl);
    }

    @Test
    public void testExpandUrl_usesHeadMethodAndUserAgent() throws IOException, InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        final URL originalUrl = mockWebServer.url("/test").url();

        // When
        UrlExpander.expandUrl(originalUrl);

        // Then
        final okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("HEAD", request.getMethod());
        assertEquals(
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36",
                request.getHeader("User-Agent"));
    }
}
