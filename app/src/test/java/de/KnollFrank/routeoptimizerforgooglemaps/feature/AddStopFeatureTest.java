package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.MapsContext;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;

@RunWith(RobolectricTestRunner.class)
public class AddStopFeatureTest {

    private AddStopFeature feature;
    private RouteUrlRequester urlRequester;

    @Before
    public void setUp() {
        final AccessibilityService service = mock(AccessibilityService.class);
        urlRequester = mock(RouteUrlRequester.class);
        final MapsContext mapsContext = new MapsContext(
                Set.of("Add stops"),
                Set.of("stops"),
                List.of(Pattern.compile("(\\d+) stops"))
        );
        feature = new AddStopFeature(service, mapsContext, urlRequester);
    }

    @Test
    public void testOnMapsEvent_ClickAddStops_TriggersUrlRequest() {
        // Given
        feature.onStopCountUpdated(15, new Rect()); // Above limit
        final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED);
        event.getText().add("Add stops");

        // When
        feature.onMapsEvent(event, null);

        // Then
        verify(urlRequester).requestUrl(any());
    }
}
