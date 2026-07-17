package de.KnollFrank.routeoptimizerforgooglemaps;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.slider.Slider;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SubscriptionUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testSliderInteractionAndBottomSheet() {
        // Initially at 15
        onView(withId(R.id.btnGenerateTemplate)).check(matches(withText(R.string.open_in_google_maps)));

        // Move slider to 16
        onView(withId(R.id.sliderTotalStops)).perform(setSliderValue(16.0f));

        // Text should still be "Open..." but button is gold (visual only, hard to test tint easily without custom matcher)
        onView(withId(R.id.btnGenerateTemplate)).check(matches(withText(R.string.open_in_google_maps)));

        // Click to show Bottom Sheet
        onView(withId(R.id.btnGenerateTemplate)).perform(click());

        // Check if BS title is visible
        onView(withText(R.string.subscription_bs_title)).check(matches(isDisplayed()));

        // Click subscribe in BS
        onView(withId(R.id.btnBsSubscribe)).perform(click());

        // Button should now be back to default state (no icon)
        onView(withId(R.id.btnGenerateTemplate)).check(matches(withText(R.string.open_in_google_maps)));
    }

    @Test
    public void testRangeIndicatorsAreVisible() {
        onView(withId(R.id.llRangeIndicator)).check(matches(isDisplayed()));
    }

    public static androidx.test.espresso.ViewAction setSliderValue(final float value) {
        return new androidx.test.espresso.ViewAction() {

            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set Slider value to " + value;
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                ((Slider) view).setValue(value);
            }
        };
    }
}
