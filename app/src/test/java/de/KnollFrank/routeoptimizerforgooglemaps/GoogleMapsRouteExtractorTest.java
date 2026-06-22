package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class GoogleMapsRouteExtractorTest {

	@Test
	public void testExtractRouteFromDirectionsUrl_AllCoordinatesInData() throws GoogleMapsRouteExtractor.MissingCoordinateException, MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0");

		// When
		final GoogleMapsRouteExtractor.Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		// Check Stop 1 (Central-Apotheke)
		assertEquals(48.4765345, route.stops().get(0).lat, 0.000001);
		assertEquals(8.934900899999999, route.stops().get(0).lng, 0.000001);
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", route.stops().get(0).placeId);

		// Check Stop 2 (Hamburg)
		assertEquals(53.548828199999996, route.stops().get(1).lat, 0.000001);
		assertEquals(9.987170299999999, route.stops().get(1).lng, 0.000001);
		assertEquals("ChIJuRMYfoNhsUcRoDrWe_I9JgQ", route.stops().get(1).placeId);
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_AllCoordinatesInPath() throws GoogleMapsRouteExtractor.MissingCoordinateException, MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0/dir/48.5015274,8.9932287/48.4765345,8.9349009/48.4752669,8.9284933/@48.4884161,8.9604793,13z/data=!4m4!4m3!2m1!2b1!3e0");

		// When
		final GoogleMapsRouteExtractor.Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		assertEquals(48.5015274, route.stops().get(0).lat, 0.000001);
		assertEquals(8.9932287, route.stops().get(0).lng, 0.000001);
		assertNull(route.stops().get(0).placeId);

		assertEquals(48.4765345, route.stops().get(1).lat, 0.000001);
		assertEquals(8.9349009, route.stops().get(1).lng, 0.000001);
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_MissingCoordinates() throws MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0/dir/48.4820178,8.9373542/Central-Apotheke,+Marktstra%C3%9Fe+17,+72108+Rottenburg+am+Neckar/Am+Berg+9,+72181+Starzach/@48.454927,8.8748639,11z/data=!4m11!4m10!1m0!1m2!1m1!1s0x4799fc4b13515dd5:0x345201aaff119b3a!1m2!1m1!1s0x4797544e94af23df:0x4bcdf7205ebe2426!2m1!2b1!3e0");

		// When & Then
		final GoogleMapsRouteExtractor.MissingCoordinateException exception =
				assertThrows(
						GoogleMapsRouteExtractor.MissingCoordinateException.class,
						() -> GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url));
		assertTrue(exception.getMessage().contains("Stop 2"));
		assertTrue(exception.getMessage().contains("Central-Apotheke"));
	}

	@Test
	public void testExtractRouteFromDirectionsUrl_Directions_UrlWithTrailingQueryParams() throws GoogleMapsRouteExtractor.MissingCoordinateException, MalformedURLException {
		// Given
		final URL url = new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0");

		// When
		final GoogleMapsRouteExtractor.Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url);

		// Then
		assertEquals(3, route.stops().size());

		// Check Stop 1
		assertEquals("Central-Apotheke", route.stops().get(0).pathName);
		assertEquals(48.4765345, route.stops().get(0).lat, 0.000001);
		assertEquals(8.934900899999999, route.stops().get(0).lng, 0.000001);
		assertEquals("ChIJ1V1RE0v8mUcROpsR_6oBUjQ", route.stops().get(0).placeId);

		// Check Stop 3 (Unterhausen)
		assertEquals("Unterhausen", route.stops().get(2).pathName);
		assertEquals(48.430628399999996, route.stops().get(2).lat, 0.000001);
		assertEquals(9.2546378, route.stops().get(2).lng, 0.000001);
		assertEquals("ChIJsYBbyF7zmUcREc3DW6XSMuQ", route.stops().get(2).placeId);
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