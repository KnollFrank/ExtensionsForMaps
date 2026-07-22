package de.KnollFrank.routeoptimizerforgooglemaps.accessibility;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@RunWith(RobolectricTestRunner.class)
public class StopCountDetectorTest {

    private StopCountDetector detector;
    private StopCountDetector.StopCountListener listener;

    @Before
    public void setUp() {
        final MapsContext mapsContext =
                new MapsContext(
                        "Add stops",
                        "stops",
                        Pattern.compile("(\\d+) stops")
                );
        detector = new StopCountDetector(mapsContext);
        listener = mock(StopCountDetector.StopCountListener.class);
        detector.addListener(listener);
    }

    @Test
    public void testDetect_Success() {
        // Given
        final AccessibilityNodeInfo root = mock(AccessibilityNodeInfo.class);
        final AccessibilityNodeInfo node = mock(AccessibilityNodeInfo.class);
        when(root.findAccessibilityNodeInfosByText("stops")).thenReturn(List.of(node));
        when(node.getText()).thenReturn("15 stops");

        // When
        detector.detect(root);

        // Then
        verify(listener).onStopCountUpdated(eq(15), any(Rect.class));
    }

    @Test
    public void testDetect_Lost() {
        // Given
        final AccessibilityNodeInfo root = mock(AccessibilityNodeInfo.class);
        when(root.findAccessibilityNodeInfosByText("stops")).thenReturn(Collections.emptyList());

        // When
        detector.detect(root);

        // Then
        verify(listener).onStopCountLost();
    }
}
