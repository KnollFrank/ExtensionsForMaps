package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import android.content.res.ColorStateList;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MainActivitySubscriptionTest {

    @Test
    public void testButtonState_FreeRange_NoSubscription() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Slider slider = activity.findViewById(R.id.sliderTotalStops);
                MaterialButton button = activity.findViewById(R.id.btnGenerateTemplate);

                // Set slider to 15 (Free limit)
                slider.setValue(15.0f);

                assertEquals(activity.getString(R.string.open_in_google_maps), button.getText().toString());
            });
        }
    }

    @Test
    public void testButtonState_PremiumRange_NoSubscription() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Slider slider = activity.findViewById(R.id.sliderTotalStops);
                MaterialButton button = activity.findViewById(R.id.btnGenerateTemplate);

                // Set slider to 16 (Premium start)
                slider.setValue(16.0f);

                assertEquals(activity.getString(R.string.unlock_premium), button.getText().toString());
                
                ColorStateList expectedTint = ContextCompat.getColorStateList(activity, R.color.color_premium_range);
                assertEquals(expectedTint, button.getBackgroundTintList());
            });
        }
    }

    @Test
    public void testButtonState_PremiumRange_AfterSubscription() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Slider slider = activity.findViewById(R.id.sliderTotalStops);
                MaterialButton button = activity.findViewById(R.id.btnGenerateTemplate);

                // Set slider to 16
                slider.setValue(16.0f);
                assertEquals(activity.getString(R.string.unlock_premium), button.getText().toString());

                // Perform "purchase" (click button in simulation mode)
                button.performClick();

                // Button should revert to "Open in Google Maps"
                assertEquals(activity.getString(R.string.open_in_google_maps), button.getText().toString());
            });
        }
    }
}
