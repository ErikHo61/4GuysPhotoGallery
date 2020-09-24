package com.example.a4guysphotogallery;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void listGoesOverTheFold() {
        onView(withId(R.id.captionText)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.captionText)).perform(typeText("This is a Test"), closeSoftKeyboard());
        onView(withId(R.id.captionBtn)).perform(click());
        onView(withId(R.id.captionText)).check(matches(withText("This is a Test")));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.keySchText)).perform(typeText("This is a Test"), closeSoftKeyboard());
        onView(withId(R.id.keySchBtn)).perform(click());
        onView(withId(R.id.captionText)).check(matches(withText("This is a Test")));
    }

}