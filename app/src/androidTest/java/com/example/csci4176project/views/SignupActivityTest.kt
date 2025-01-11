package com.example.csci4176project.views

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.csci4176project.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(SignupActivity::class.java)

    @Test
    fun testSignup_Successful() {
        // Enter valid credentials
        onView(withId(R.id.usernameBox)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordBox)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.reenterBox)).perform(typeText("password123"), closeSoftKeyboard())

        // Click on the signup button
        onView(withId(R.id.signup_button)).perform(click())
    }

    @Test
    fun testSignup_PasswordMismatch() {
        // Enter valid username but mismatching passwords
        onView(withId(R.id.usernameBox)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordBox)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.reenterBox)).perform(typeText("password456"), closeSoftKeyboard())

        // Click on the signup button
        onView(withId(R.id.signup_button)).perform(click())
    }

    @Test
    fun testSignup_PasswordTooShort() {
        // Enter valid username but short password
        onView(withId(R.id.usernameBox)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordBox)).perform(typeText("123"), closeSoftKeyboard())
        onView(withId(R.id.reenterBox)).perform(typeText("123"), closeSoftKeyboard())

        // Click on the signup button
        onView(withId(R.id.signup_button)).perform(click())
    }

}
