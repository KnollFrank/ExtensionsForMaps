package de.KnollFrank.routeoptimizerforgooglemaps;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

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
    public void testSliderInteractionAndButtonUpdate() {
        // Initially at 15
        onView(withId(R.id.btnGenerateTemplate)).check(matches(withText(R.string.open_in_google_maps)));

        // Move slider to 16
        onView(withId(R.id.sliderTotalStops)).perform(setSliderValue(16.0f));

        // Check if button changed to "Unlock..." (contains first line)
        onView(withId(R.id.btnGenerateTemplate)).check(matches(withText(containsString("Unlock 16"))));

        // Click to "purchase" (Simulation mode assumed)
        onView(withId(R.id.btnGenerateTemplate)).perform(click());

        // Button should revert
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
