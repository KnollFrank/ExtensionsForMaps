package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.regex.Pattern;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;

@RunWith(RobolectricTestRunner.class)
public class AddStopFeatureTest {

    private AddStopFeature feature;
    private RouteUrlRequester urlRequester;
    private RouteUrlRequester.RouteUrlCallback callback;

    @Before
    public void setUp() {
        final AccessibilityService service = mock(AccessibilityService.class);
        urlRequester = mock(RouteUrlRequester.class);
        callback = mock(RouteUrlRequester.RouteUrlCallback.class);
        final GoogleMapsContext googleMapsContext =
                new GoogleMapsContext(
                        "Add stops",
                        "stops",
                        Pattern.compile("(\\d+) stops"));
        feature = new AddStopFeature(service, googleMapsContext, urlRequester, callback);
    }

    @Test
    public void testOnGoogleMapsEvent_ClickAddStops_TriggersUrlRequest() {
        // Given
        feature.onStopCountUpdated(15, new Rect()); // Above limit
        final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED);
        event.getText().add("Add stops");

        // When
        feature.onGoogleMapsEvent(event, null);

        // Then
        verify(urlRequester).requestRouteUrl(callback);
    }
}
