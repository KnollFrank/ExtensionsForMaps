package de.knollfrank.extensionsformaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
public class WebUrlLauncherTest {

    @Test
    public void testOpenUrl_startsViewIntent() {
        // Given
        final Application context = ApplicationProvider.getApplicationContext();
        final String testUrl = "https://paypal.me/KnollFrank";

        // When
        WebUrlLauncher.openUrl(context, testUrl);

        // Then
        final ShadowApplication shadowApp = Shadows.shadowOf(context);
        final Intent nextStartedActivity = shadowApp.getNextStartedActivity();

        assertNotNull(nextStartedActivity);
        assertEquals(Intent.ACTION_VIEW, nextStartedActivity.getAction());
        assertEquals(testUrl, nextStartedActivity.getDataString());
    }
}
