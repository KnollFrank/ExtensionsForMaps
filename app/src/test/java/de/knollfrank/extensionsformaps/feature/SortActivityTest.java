package de.knollfrank.extensionsformaps.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import de.knollfrank.extensionsformaps.repository.AccumulatedStopsRepository;

@RunWith(RobolectricTestRunner.class)
public class SortActivityTest {

    private static final String TEST_URL =
            "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0&g_ep=CAESCDI2LjE2LjEyGAAgkUEqiwEsOTQyNjc3MjcsOTQyOTIxOTUsOTQyOTk1MzIsMTAwNzk2NDk4LDEwMDc5Nzc2MSwxMDA3OTY1MzUsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA6LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5QgJVUw%3D%3D&skid=0a1f62d3-c01c-47b9-b4b6-ccadc456baa8";

    @Test
    public void testProcessSharedUrl_accumulatesStopsWithoutMainThreadDbException() throws Exception {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, TEST_URL);

        final Context context = ApplicationProvider.getApplicationContext();

        try (final ActivityController<SortActivity> controller = Robolectric.buildActivity(SortActivity.class, intent).setup()) {
            final SortActivity activity = controller.get();
            assertNotNull(activity);

            // Give background tasks in ForkJoinPool a moment to run and post to main looper
            for (int i = 0; i < 50; i++) {
                ShadowLooper.idleMainLooper();
                if (CompletableFuture.supplyAsync(repo(context)::hasAccumulatedStops).get(1, TimeUnit.SECONDS)) {
                    break;
                }
                Thread.sleep(20);
            }

            final boolean hasStops = CompletableFuture.supplyAsync(repo(context)::hasAccumulatedStops).get(1, TimeUnit.SECONDS);
            final int count = CompletableFuture.supplyAsync(repo(context)::getAccumulatedStopsCount).get(1, TimeUnit.SECONDS);

            assertTrue(hasStops);
            assertEquals(3, count);
        }
    }

    private static AccumulatedStopsRepository repo(final Context context) {
        return new AccumulatedStopsRepository(context);
    }
}
