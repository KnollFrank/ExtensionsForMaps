package de.knollfrank.extensionsformaps;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class ProgressOverlayTest {

    private ProgressOverlay progressOverlay;

    @Before
    public void setUp() {
        final Context context = ApplicationProvider.getApplicationContext();
        progressOverlay = new ProgressOverlay(context);
    }

    @Test
    public void testShow() {
        // When
        progressOverlay.show();
        ShadowLooper.idleMainLooper();

        // Then (no crash)
    }

    @Test
    public void testHide() {
        // Given
        progressOverlay.show();
        ShadowLooper.idleMainLooper();

        // When
        progressOverlay.hide();
        ShadowLooper.idleMainLooper();

        // Then (no crash)
    }
}
