package com.example.csci4176project.views

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.csci4176project.util.CurrentUser
import com.example.csci4176project.R
import com.example.csci4176project.components.CurrencyUpdater
import com.example.csci4176project.repositories.UserRepository
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class FinishSetUpActivity : AppCompatActivity() {

    // Initialize the UI elements
    private lateinit var profileName: EditText
    private lateinit var submitButton: Button
    private lateinit var laterButton: Button
    private lateinit var profilePicture : ImageView
    private lateinit var imageString : String
    private lateinit var toggleCurrency: SwitchMaterial
    private var newImage: Boolean = false
    private val storage = FirebaseStorage.getInstance()
    private lateinit var currencyUpdater: CurrencyUpdater
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_setup)

        var imageSelected = false
        var nameChosen = false
        var selectedCurrency = "USD"

        val bundle : Bundle? = intent.extras
        val edit = bundle?.getString("edit")

        val toggleCurrency: SwitchMaterial = findViewById(R.id.toggleCurrency)

        // Set the UI elements
        profileName = findViewById(R.id.profileName)
        submitButton = findViewById(R.id.submitButton)
        laterButton = findViewById(R.id.skipButton)
        profilePicture = findViewById(R.id.profilePicture)
        currencyUpdater = CurrencyUpdater(this)

        spinner = findViewById(R.id.currency)
        // Create an ArrayAdapter
       adapter = ArrayAdapter.createFromResource(
            this,
            R.array.currencyOptions,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Restore toggle state
        val savedToggleState = sharedPreferences.getBoolean("toggle_state_" + CurrentUser.userId, false)
        toggleCurrency.isChecked = savedToggleState

        if (edit == CurrentUser.userId.toString()){
            laterButton.setText("Go Back")
            UserRepository.read(CurrentUser.userId!!) {user ->
                profileName.setText(user?.name)
                spinner.setSelection(resources.getStringArray(R.array.currencyOptions).indexOf(user?.currency))
                val id = user?.pfp?.substringAfterLast("/")
                val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
                Picasso.get().load(imageUrl)
                    .error(R.drawable.default_profile_picture)
                    .fit()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(profilePicture)
                imageString = user?.pfp.toString()
            }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedCurrency = p0?.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedCurrency = "USD"
            }
        }

        if(edit == CurrentUser.userId.toString()){
            setupSwitch(savedToggleState)
        }

        toggleCurrency.setOnCheckedChangeListener { _, isChecked ->
            setupSwitch(isChecked)
        }

        submitButton.setOnClickListener {
            // Submit data
            nameChosen = profileName.text.toString() != ""
            imageSelected = profilePicture.drawable != null

            if (imageSelected && nameChosen) {
                if (edit == CurrentUser.userId.toString() && !newImage){
                    if (CurrentUser.userId != null) {
                        UserRepository.update(CurrentUser.userId!!,profileName.text.toString(),
                            CurrentUser.email!!, imageString, selectedCurrency)
                        navigateProfile()
                    } else {
                        Toast.makeText(this, "Update Failed.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val profIMRef = uploadImage()

                    if (CurrentUser.userId != null) {
                        UserRepository.update(CurrentUser.userId!!,profileName.text.toString(),
                            CurrentUser.email!!, profIMRef.toString(), selectedCurrency)
                    }

                    if (edit == CurrentUser.userId.toString()) {
                        navigateProfile()
                    } else {
                        navigateDashboard()
                    }
                }
            } else {
                Toast.makeText(this, "Image and name required.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the click listener for the later button
        laterButton.setOnClickListener {
            if (edit == CurrentUser.userId.toString()) {
                Toast.makeText(this, "No changes applied to profile.", Toast.LENGTH_SHORT).show()
                navigateProfile()
            } else if (CurrentUser.userId != null) {
                UserRepository.update(CurrentUser.userId!!, CurrentUser.email!!,
                    CurrentUser.email!!, "default", selectedCurrency)
                navigateDashboard()
            }
        }

        // Set the click listener for the profile picture: TODO - setup firebase submission later
        profilePicture.setOnClickListener {
            // Check if permission to access images is granted - if not, request it
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
            // Create an intent to open the gallery
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }
    }

    fun setupSwitch(isChecked: Boolean){

        if (isChecked) {
            // Set the spinner to a specific state or value when grayed out
            // Example: Setting spinner to a default or disabled state

            currencyUpdater.initialize(){curr ->
                val pos = adapter.getPosition(curr)
                Log.d("test", curr)
                spinner.setSelection(pos)
                saveToggleState(true)
                spinner.isEnabled = false
            }

            // set spinner to a specific selection

        } else {
            // If not checked, the spinner will be interactive as usual, and specific logic can be applied as needed
            saveToggleState(false)
            spinner.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        currencyUpdater.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun saveToggleState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("toggle_state_" + CurrentUser.userId, state)
        editor.apply()
    }

    private fun uploadImage(): StorageReference {
        // Create a storage reference from our app
        val storageRef = storage.reference

        // Create a reference to "mountains.jpg"
        val profIM = storageRef.child("${CurrentUser.userId}.jpg")

        // Create a reference to 'images/mountains.jpg'
        val profIMRef = storageRef.child("images/${CurrentUser.userId}.jpg")

        // Get the data from an ImageView as bytes
        profilePicture.isDrawingCacheEnabled = true
        profilePicture.buildDrawingCache()
        val bitmap = (profilePicture.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = profIM.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
        return profIMRef
    }

    // This function is called when the user selects an image from the gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Set the image view to the selected image

            val selectedImage = data.data
            profilePicture.setImageURI(selectedImage)
            newImage = true
        }
    }

    private fun navigateDashboard() {
        val dashboardIntent = Intent(this, GroupsActivity::class.java)
        startActivity(dashboardIntent)
    }
    private fun navigateProfile() {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        startActivity(profileIntent)
        finishAffinity()
    }


}
