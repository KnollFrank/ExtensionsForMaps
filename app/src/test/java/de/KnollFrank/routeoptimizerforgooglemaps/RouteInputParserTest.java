package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class RouteInputParserTest {

    @Test
    public void testExtractUrl_withSurroundingText() {
        final String input = "Hier ist die Route für unseren Ausflug: https://maps.google.com/dir/Tübingen/Mailand \nViel Spaß!";
        final String expected = "https://maps.google.com/dir/Tübingen/Mailand";
        assertEquals(expected, RouteInputParser.extractUrl(input));
    }

    @Test
    public void testParseAddresses_withUrl() throws UnsupportedEncodingException {
        final String shareText = "Brandenburger Tor \n Pariser Platz, 10117 Berlin \n https://maps.app.goo.gl/xyz123";
        final List<String> result = RouteInputParser.parseAddresses(shareText);
        assertEquals(1, result.size());
        assertEquals("Brandenburger Tor, Pariser Platz, 10117 Berlin", result.get(0));
    }

    @Test
    public void testParseAddresses_multiStopUrl() throws UnsupportedEncodingException {
        final String multiUrl = "https://www.google.com/maps/dir/Berlin/Leipzig/Munich/data=!4m2!3m1!1s0x...";
        final List<String> result = RouteInputParser.parseAddresses(multiUrl);
        assertEquals(List.of("Berlin", "Leipzig", "Munich"), result);
    }

    @Test
    public void testParseAddresses_shareDirectionsText() throws UnsupportedEncodingException {
        final String text = "Shared route\nFrom Berlin to Munich\n\n1. Head south\n4. Arrive at location: Leipzig\n12. Arrive at location: Munich";
        final List<String> result = RouteInputParser.parseAddresses(text);
        assertEquals(List.of("Leipzig", "Munich"), result);
    }

    @Test
    public void testParseAddresses_longGoogleMapsUrl() throws UnsupportedEncodingException {
        final String longUrl = "https://www.google.com/maps/place/Goetheanum,+R%C3%BCttiweg+45,+4143+Dornach,+Switzerland/data=!4m2!3m1!1s0x4791c871e14eee41:0x28bc250fed7c57c";
        final List<String> result = RouteInputParser.parseAddresses(longUrl);
        assertEquals(List.of("Goetheanum, Rüttiweg 45, 4143 Dornach, Switzerland"), result);
    }

    @Test
    public void testParseAddresses_reproductionUserBug() throws UnsupportedEncodingException {
        final String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0";
        final List<String> result = RouteInputParser.parseAddresses(url);
        // Note: parseAddresses returns the labels from path
        assertEquals(List.of("Central-Apotheke", "Hamburg", "Unterhausen"), result);
    }

    @Test
    public void testExtractStopsFromUrl_userBug_Exact() {
        final String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0";
        final List<RouteOptimizer.Stop> stops = RouteInputParser.extractStopsFromUrl(url);

        // Printing for debugging
        for (RouteOptimizer.Stop s : stops) {
            System.out.println("DEBUG: Extracted stop: " + s.address() + " at " + s.lat() + "," + s.lng());
        }

        assertEquals(3, stops.size());
    }
}