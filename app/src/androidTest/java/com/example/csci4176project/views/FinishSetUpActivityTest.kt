package com.example.csci4176project.views

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.csci4176project.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinishSetUpActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(FinishSetUpActivity::class.java)

    @Test
    fun testLaterButton() {
        // Click later button
        onView(withId(R.id.skipButton)).perform(click())
    }

    @Test
    fun testProfilePictureSelection() {
        // Click profile picture
        onView(withId(R.id.profilePicture)).perform(click())
    }
}
