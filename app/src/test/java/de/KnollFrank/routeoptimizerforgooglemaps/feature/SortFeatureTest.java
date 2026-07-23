package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import static org.mockito.Mockito.mock;

import android.accessibilityservice.AccessibilityService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.KnollFrank.routeoptimizerforgooglemaps.accessibility.RouteUrlRequester;

@RunWith(RobolectricTestRunner.class)
public class SortFeatureTest {

    private SortFeature feature;

    @Before
    public void setUp() {
        final AccessibilityService service = mock(AccessibilityService.class);
        final RouteUrlRequester urlRequester = mock(RouteUrlRequester.class);
        feature = new SortFeature(service, urlRequester);
    }

    @Test
    public void testFeatureInitialization() {
        // FK-TODO: prüfe, ob ein Klick auf den Sortbutton den OnClickListener auslöst.
        // Just verify constructor works
    }
}
