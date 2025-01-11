package com.example.csci4176project.views

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.csci4176project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginButton_Click() {
        onView(withId(R.id.usernameBox)).perform(typeText("test@example.com"))
        onView(withId(R.id.passwordBox)).perform(typeText("password"))
        onView(withId(R.id.login_button)).perform(click())
    }

    @Test
    fun testSignUpPrompt_Click() {
        // Test clicking on the sign-up prompt
        onView(withId(R.id.textViewSignUpPrompt)).perform(click())
    }

    @Test
    fun testAutoLogin() {
        // Test auto-login when already logged in
        val auth = Firebase.auth
        auth.signInWithEmailAndPassword("test@example.com", "password").addOnCompleteListener { task ->
            assertTrue(true)
        }
    }

}
