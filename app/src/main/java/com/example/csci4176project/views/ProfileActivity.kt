package com.example.csci4176project.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.TextView
import com.example.csci4176project.util.CurrentUser
import com.example.csci4176project.R
import com.example.csci4176project.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso


class ProfileActivity: AppCompatActivity() {

    private lateinit var displayName: TextView
    private lateinit var email: TextView
    private lateinit var currency: TextView
    private lateinit var logoutBtn: Button
    private lateinit var editClick: ImageView
    private lateinit var displayPicture: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        displayName = findViewById(R.id.fullnameProfile)
        email = findViewById(R.id.profileEmail)
        currency = findViewById(R.id.currencyProfile)
        logoutBtn = findViewById(R.id.LogOutButton)

        editClick = findViewById(R.id.editIcon)
        displayPicture = findViewById(R.id.profileImage)

        logoutBtn.setOnClickListener {
            // Show confirmation
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { dialog, which ->
                    FirebaseAuth.getInstance().signOut() // Log out

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
                    startActivity(intent)
                    finish() // Prevent returning to this activity
                }
                .setNegativeButton("No") { dialog, which ->
                    // Dismiss the dialog and do nothing
                }
                .show()
        }

        val notifBtn: ImageButton = findViewById(R.id.notificationIcon)
        notifBtn.setOnClickListener{
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val activityBtn: ImageButton = findViewById(R.id.activityIcon)
        activityBtn.setOnClickListener{
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        val groupBtn: ImageButton = findViewById(R.id.groupIcon)
        groupBtn.setOnClickListener{
            val intent = Intent(this, GroupsActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        editClick.setOnClickListener{
            val setup = Intent(this, FinishSetUpActivity::class.java)
            setup.putExtra("edit", CurrentUser.userId!!.toString())
            startActivity(setup)
        }

        setViews()
    }

    private fun setViews(){
        UserRepository.read(CurrentUser.userId!!) { user ->
            displayName.setText(user?.name)
            email.setText(user?.email)
            currency.setText(user?.currency)
            user?.pfp?.substringAfterLast("/").let {
                if (it != null) {
                    displayImage(it)
                }
            }
        }
    }

    private fun displayImage(id: String){
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
        Picasso.get().load(imageUrl)
            .error(R.drawable.default_profile_picture)
            .fit()
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .into(displayPicture)
    }
}

