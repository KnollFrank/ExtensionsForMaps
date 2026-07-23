package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.regex.Pattern;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.GoogleMapsContext;

@RunWith(RobolectricTestRunner.class)
public class AddStopFeatureTest {

    private AddStopFeature feature;
    private OnClickListener onClickListener;

    @Before
    public void setUp() {
        final AccessibilityService service = mock(AccessibilityService.class);
        onClickListener = mock(OnClickListener.class);
        final GoogleMapsContext googleMapsContext =
                new GoogleMapsContext(
                        "Add stops",
                        "stops",
                        Pattern.compile("(\\d+) stops"));
        feature = new AddStopFeature(service, googleMapsContext, onClickListener);
    }

    @Test
    public void testOnGoogleMapsEvent_ClickAddStops_TriggersCallback() {
        // Given
        feature.onStopCountUpdated(15, new Rect()); // Above limit
        final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED);
        event.getText().add("Add stops");

        // When
        feature.onGoogleMapsEvent(event, null);

        // Then
        verify(onClickListener).onClick(null);
    }
}
