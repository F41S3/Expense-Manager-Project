package com.example.csci4176project.views

import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.csci4176project.util.CurrentUser
import com.example.csci4176project.R
import com.example.csci4176project.ui.theme.setClickableSpan
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
class LoginActivity : AppCompatActivity() {

    // Initialize the UI elements
    private lateinit var usernameBox: EditText
    private lateinit var passwordBox: EditText
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var textViewSignUpPrompt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // Logged in -> go to profile activity (can change to main activity later)
            CurrentUser.setCurrentUser(auth.currentUser?.uid.toString())
            navigateLoggedIn()
            finish()
        }

        // Set the UI elements
        usernameBox = findViewById(R.id.usernameBox)
        passwordBox = findViewById(R.id.passwordBox)
        loginButton = findViewById(R.id.login_button)
        textViewSignUpPrompt = findViewById(R.id.textViewSignUpPrompt)

        // Click listener for the login button
        loginButton.setOnClickListener {
            // Get the username and password from the input fields
            val username = usernameBox.text.toString()
            val password = passwordBox.text.toString()

            // Simple validation + Firebase authentication
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show()
            } else {
                // If not empty, then log in
                auth = Firebase.auth
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            CurrentUser.setCurrentUser(auth.currentUser?.uid.toString())
                            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                            navigateLoggedIn()
                        } else {
                            // If sign in fails, display error to the user.
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
        }
        textViewSignUpPrompt.setClickableSpan(
            fullText = "Need an account? Sign up now",
            clickableText = "Sign up now"
        ) {
            // Action to perform when "Sign up now" is clicked
            val signUpIntent = Intent(this, SignupActivity::class.java)
            startActivity(signUpIntent)
        }
    }

    // Helper Function: Logged in -> go to profile activity
    // (can change to main activity later)
    private fun navigateLoggedIn() {
        val loggedInIntent = Intent(this, GroupsActivity::class.java)
        startActivity(loggedInIntent)
    }
}