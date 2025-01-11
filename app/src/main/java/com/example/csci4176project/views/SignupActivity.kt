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
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.ui.theme.setClickableSpan
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignupActivity : AppCompatActivity() {

    // Initialize the UI elements
    private lateinit var usernameBox: EditText
    private lateinit var passwordBox: EditText
    private lateinit var passwordRepeatBox : EditText
    private lateinit var signupButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var textViewLoginPrompt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Set the UI elements
        usernameBox = findViewById(R.id.usernameBox)
        passwordBox = findViewById(R.id.passwordBox)
        passwordRepeatBox = findViewById(R.id.reenterBox)
        textViewLoginPrompt = findViewById(R.id.textViewLoginPrompt)
        signupButton = findViewById(R.id.signup_button)


        // Set the click listener for the signup button
        signupButton.setOnClickListener {

            // Get the username and password from the input fields
            val username = usernameBox.text.toString()
            val password = passwordBox.text.toString()
            val passwordRepeat = passwordRepeatBox.text.toString()

            // Simple validation for now - add firebase later
            if (username.isEmpty() || password.isEmpty() || passwordRepeat.isEmpty()) {
                Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show()
            } else if (password != passwordRepeat) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6){
                Toast.makeText(this, "Password must be greater than 5 characters", Toast.LENGTH_SHORT).show()
            } else {
                // If the username and password are not empty, then we can sign up
                // For now, we just display a toast message
                Toast.makeText(this, "Signing up...", Toast.LENGTH_SHORT).show()
                // Initialize Firebase Auth
                auth = Firebase.auth
                auth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            UserRepository.create(auth.currentUser?.uid.toString(), username, username, "default", "USD")
                            Toast.makeText(this, "Account created.", Toast.LENGTH_SHORT).show()
                            CurrentUser.setCurrentUser(auth.currentUser?.uid.toString())

                            // After a delay, change to profile activity
                            val loggedInIntent = Intent(this, FinishSetUpActivity::class.java)
                            startActivity(loggedInIntent)

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
        }
        textViewLoginPrompt.setClickableSpan(
            fullText = "Already have an account? Login now",
            clickableText = "Login now"
        ) {
            // Action to perform when "Login now" is clicked
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

    }
}