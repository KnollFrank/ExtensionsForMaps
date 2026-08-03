package de.knollfrank.extensionsformaps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class ProgressOverlayTest {

    private ProgressOverlay progressOverlay;
    private WindowManager windowManager;

    @Before
    public void setUp() {
        final Context context = ApplicationProvider.getApplicationContext();
        windowManager = mock(WindowManager.class);
        progressOverlay = new ProgressOverlay(context, windowManager);
    }

    @Test
    public void testShow() {
        // When
        progressOverlay.show();
        ShadowLooper.idleMainLooper();

        // Then
        verify(windowManager).addView(any(View.class), any(WindowManager.LayoutParams.class));
    }

    @Test
    public void testHide() {
        // Given
        progressOverlay.show();
        ShadowLooper.idleMainLooper();

        // When
        progressOverlay.hide();
        ShadowLooper.idleMainLooper();

        // Then
        verify(windowManager).removeView(any(View.class));
    }
}
