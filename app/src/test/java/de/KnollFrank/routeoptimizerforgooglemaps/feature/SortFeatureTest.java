package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.widget.Button;
import android.widget.LinearLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import de.KnollFrank.routeoptimizerforgooglemaps.RouteOptimizerAccessibilityService;
import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;

@RunWith(RobolectricTestRunner.class)
public class SortFeatureTest {

    private SortFeature feature;
    private RouteUrlRequester urlRequester;
    private RouteUrlRequester.RouteUrlCallback callback;

    @Before
    public void setUp() {
        final AccessibilityService service = Robolectric.setupService(RouteOptimizerAccessibilityService.class);
        urlRequester = mock(RouteUrlRequester.class);
        callback = mock(RouteUrlRequester.RouteUrlCallback.class);
        feature = new SortFeature(service, urlRequester, callback);
    }

    @Test
    public void testSortButtonClick_requestsRouteUrl() {
        // Given
        feature.onStopCountUpdated(15, new Rect(0, 0, 100, 50));
        final LinearLayout container = (LinearLayout) feature.getSortButtonOverlay();
        final Button sortButton = (Button) container.getChildAt(0);

        // When
        sortButton.performClick();

        // Then
        verify(urlRequester).requestRouteUrl(callback);
    }
}
