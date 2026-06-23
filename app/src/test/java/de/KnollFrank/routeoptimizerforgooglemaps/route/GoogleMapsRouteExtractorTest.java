package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public class GoogleMapsRouteExtractorTest {

	@Test
	public void testExtractRouteFromDirectionsUrl_AllCoordinatesInData() throws MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0");

		// When
		final Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		// Check Stop 1 (Central-Apotheke)
		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(48.4765345, DEGREES),
						new Angle(8.934900899999999, DEGREES)),
				route.stops().get(0).geodetic());
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", route.stops().get(0).placeId().orElseThrow());

		// Check Stop 2 (Hamburg)
		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(53.548828199999996, DEGREES),
						new Angle(9.987170299999999, DEGREES)),
				route.stops().get(1).geodetic());
		assertEquals("ChIJuRMYfoNhsUcRoDrWe_I9JgQ", route.stops().get(1).placeId().orElseThrow());
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_AllCoordinatesInPath() throws MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0");

		// When
		final Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(48.5015274, DEGREES),
						new Angle(8.9932287, DEGREES)),
				route.stops().get(0).geodetic());
		assertTrue(route.stops().get(0).placeId().isEmpty());

		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(48.4765345, DEGREES),
						new Angle(8.9349009, DEGREES)),
				route.stops().get(1).geodetic());
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_MissingCoordinates() throws MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0");

		// When & Then
		assertThrows(
				IllegalArgumentException.class,
				() -> GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url));
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_Directions_UrlWithTrailingQueryParams() throws MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0");

		// When
		final Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		// Check Stop 1
		assertEquals("Central-Apotheke", route.stops().get(0).pathName());
		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(48.4765345, DEGREES),
						new Angle(8.934900899999999, DEGREES)),
				route.stops().get(0).geodetic());
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", route.stops().get(0).placeId().orElseThrow());

		// Check Stop 3 (Unterhausen)
		assertEquals("Unterhausen", route.stops().get(2).pathName());
		assertEquals(
				Geodetic.fromLatitudeLongitude(
						new Angle(48.430628399999996, DEGREES),
						new Angle(9.2546378, DEGREES)),
				route.stops().get(2).geodetic());
		assertEquals("ChIJsYBbyF7zmUcREc3DW6XSMuQ", route.stops().get(2).placeId().orElseThrow());
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_InvalidDirectionsUrlFormat() throws MalformedURLException {
		// Given
		final URL invalidUrl = new URL("https://www.google.com/maps/place/Berlin/data=!3m1!4b1!4m6!3m5!1s0x47a84e373f035901:0x42120465b5e3b70/place/Central-Apotheke/");

		// When & Then
		final IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(invalidUrl));
		assertTrue(exception.getMessage().contains("Invalid URL"));
	}
}