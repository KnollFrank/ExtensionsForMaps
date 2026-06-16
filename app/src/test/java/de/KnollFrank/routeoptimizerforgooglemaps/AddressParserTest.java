package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class AddressParserTest {

	@Test
	public void testParseAddresses_withUrl() {
		final String shareText = "Brandenburger Tor \n Pariser Platz, 10117 Berlin \n https://maps.app.goo.gl/xyz123";
		final List<String> result = MainActivity.parseAddresses(shareText);
		assertEquals(1, result.size());
		assertEquals("Brandenburger Tor, Pariser Platz, 10117 Berlin", result.get(0));
	}

	@Test
	public void testParseAddresses_multiStopUrl() {
		final String multiUrl = "https://www.google.com/maps/dir/Berlin/Leipzig/Munich/data=!4m2!3m1!1s0x...";
		final List<String> result = MainActivity.parseAddresses(multiUrl);
		assertEquals(List.of("Berlin", "Leipzig", "Munich"), result);
	}

	@Test
	public void testParseAddresses_shareDirectionsText() {
		final String text = "Shared route\nFrom Berlin to Munich\n\n1. Head south\n4. Arrive at location: Leipzig\n12. Arrive at location: Munich";
		final List<String> result = MainActivity.parseAddresses(text);
		assertEquals(List.of("Leipzig", "Munich"), result);
	}

	@Test
	public void testParseAddresses_longGoogleMapsUrl() {
		final String longUrl = "https://www.google.com/maps/place/Goetheanum,+R%C3%BCttiweg+45,+4143+Dornach,+Switzerland/data=!4m2!3m1!1s0x4791c871e14eee41:0x28bc250fed7c57c";
		final List<String> result = MainActivity.parseAddresses(longUrl);
		assertEquals(List.of("Goetheanum, Rüttiweg 45, 4143 Dornach, Switzerland"), result);
	}

	@Test
	public void testParseAddresses_reproductionUserBug() {
		final String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0";
		final List<String> result = MainActivity.parseAddresses(url);
		// Note: parseAddresses returns the labels from path
		assertEquals(List.of("Central-Apotheke", "Hamburg", "Unterhausen"), result);
	}

	@Test
	public void testExtractStopsFromUrl_userBug() {
		final String url = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0";
		final List<RouteOptimizer.Stop> stops = MainActivity.extractStopsFromUrl(url);

		assertEquals(3, stops.size());
		assertEquals("Central-Apotheke", stops.get(0).address());
		assertEquals(48.4765345, stops.get(0).lat(), 0.000001);
		assertEquals(8.9349008, stops.get(0).lng(), 0.000001);

		assertEquals("Hamburg", stops.get(1).address());
		assertEquals(53.548828, stops.get(1).lat(), 0.000001);

		assertEquals("Unterhausen", stops.get(2).address());
		assertEquals(48.430628, stops.get(2).lat(), 0.000001);
	}
}
