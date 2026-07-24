package de.KnollFrank.routeoptimizerforgooglemaps;

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
// FK-TODO: macht das Sinn?
public class SpinnerOverlayTest {

    private SpinnerOverlay spinnerOverlay;
    private WindowManager windowManager;

    @Before
    public void setUp() {
        final Context context = ApplicationProvider.getApplicationContext();
        windowManager = mock(WindowManager.class);
        spinnerOverlay = new SpinnerOverlay(context, windowManager);
    }

    @Test
    public void testShow() {
        // When
        spinnerOverlay.show();
        ShadowLooper.idleMainLooper();

        // Then
        verify(windowManager).addView(any(View.class), any(WindowManager.LayoutParams.class));
    }

    @Test
    public void testHide() {
        // Given
        spinnerOverlay.show();
        ShadowLooper.idleMainLooper();

        // When
        spinnerOverlay.hide();
        ShadowLooper.idleMainLooper();

        // Then
        verify(windowManager).removeView(any(View.class));
    }
}
