package de.knollfrank.extensionsformaps.accessibility;

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

import java.util.List;
import java.util.regex.Pattern;

@RunWith(RobolectricTestRunner.class)
public class StopCountDetectorTest {

    private StopCountDetector stopCountDetector;
    private StopCountDetector.StopCountListener stopCountListener;

    @Before
    public void setUp() {
        final GoogleMapsContext googleMapsContext =
                new GoogleMapsContext(
                        "Add stops",
                        "stops",
                        new StopCountParser(Pattern.compile("(\\d+) stops")));
        stopCountListener = mock(StopCountDetector.StopCountListener.class);
        stopCountDetector = new StopCountDetector(googleMapsContext, List.of(stopCountListener));
    }

    @Test
    public void testDetectStopCount_Success() {
        // Given
        final AccessibilityNodeInfo root = mock(AccessibilityNodeInfo.class);
        final AccessibilityNodeInfo node = mock(AccessibilityNodeInfo.class);
        when(root.findAccessibilityNodeInfosByText("stops")).thenReturn(List.of(node));
        when(node.getText()).thenReturn("15 stops");

        // When
        stopCountDetector.detectStopCount(root);

        // Then
        verify(stopCountListener).onStopCountUpdated(eq(15), any(Rect.class));
    }

    @Test
    public void testDetectStopCount_Lost() {
        // Given
        final AccessibilityNodeInfo root = mock(AccessibilityNodeInfo.class);
        when(root.findAccessibilityNodeInfosByText("stops")).thenReturn(List.of());

        // When
        stopCountDetector.detectStopCount(root);

        // Then
        verify(stopCountListener).onStopCountLost();
    }
}
