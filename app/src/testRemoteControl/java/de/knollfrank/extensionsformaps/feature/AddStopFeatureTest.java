package de.knollfrank.extensionsformaps.feature;

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

import de.knollfrank.extensionsformaps.accessibility.GoogleMapsContext;
import de.knollfrank.extensionsformaps.accessibility.RouteUrlRequester;
import de.knollfrank.extensionsformaps.accessibility.StopCountParser;

@RunWith(RobolectricTestRunner.class)
public class AddStopFeatureTest {

    private AddStopFeature addStopFeature;
    private RouteUrlRequester routeUrlRequester;
    private RouteUrlRequester.RouteUrlCallback routeUrlCallback;

    @Before
    public void setUp() {
        routeUrlRequester = mock(RouteUrlRequester.class);
        routeUrlCallback = mock(RouteUrlRequester.RouteUrlCallback.class);
        addStopFeature =
                new AddStopFeature(
                        mock(AccessibilityService.class),
                        new GoogleMapsContext(
                                "Add stops",
                                "stops",
                                new StopCountParser(Pattern.compile("(\\d+) stops"))),
                        routeUrlRequester,
                        routeUrlCallback);
    }

    @Test
    public void testOnGoogleMapsEvent_ClickAddStops_TriggersUrlRequest() {
        // Given
        addStopFeature.onStopCountUpdated(15, new Rect()); // Above limit
        final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED);
        event.getText().add("Add stops");

        // When
        addStopFeature.onGoogleMapsEvent(event, null);

        // Then
        verify(routeUrlRequester).requestRouteUrl(routeUrlCallback);
    }
}
