package com.example.csci4176project.components

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.csci4176project.appPermissions.LocationPermissions
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Currency
import java.util.Locale

class CurrencyUpdater(private val activity: Activity) {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private lateinit var permission: LocationPermissions
    private val user = FirebaseAuth.getInstance().currentUser
    private val uid = user?.uid

    // Initialize the currency updater
    fun initialize(callback: (String) -> Unit) {
        // Initialize LocationPermissions
        permission = LocationPermissions()
        // Check if location permission is granted
        if (permission.isLocationPermissionGranted(activity)) {
            // Request currency updates if permission is granted
            requestCurrencyUpdates(){curr->
                if (curr != null) {
                    callback(curr)
                }
            }
        } else {
            // Request location permission if not granted
            permission.requestLocationPermission(activity, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Handle permission request results
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission granted, request currency updates
                requestCurrencyUpdates(){currency->
                    // no need to use currency
                }
            } else {
                // If permission denied, show permission denied dialog
                showPermissionDeniedDialog()
            }
        }
    }

    // Show permission denied dialog
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Denied")
            .setMessage("Location permission is required to access your current location. Please grant the permission in the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Open app settings
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    // Request currency updates
    private fun requestCurrencyUpdates(callback: (String?) -> Unit) {
        // Check if location permission is granted
        var currency = ""
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission not granted, show permission denied dialog
            showPermissionDeniedDialog()
            callback(currency)
        }

        // Get last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // If location found, get country and currency info
                    val country = getCountryFromLocation(location)
                    Log.i("Detected Country",country)
                    currency = getCurrencyFromCountry(country)
                    CurrentUser.currency = currency
                    updateCurrencyToDb(currency)
                    Log.i("Country's Currency", currency)
                    callback(currency)
                } else {
                    // If location not found, show error message
                    Log.e("Location getting error", "Cannot get location")
                }
            }
    }

    // Get country from location
    private fun getCountryFromLocation(location: Location): String {
        val geocoder = Geocoder(activity, Locale.getDefault())

        return try {
            //val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            // fake address for testing get the latitude and longitude from https://www.latlong.net/
           val addresses = geocoder.getFromLocation(51.507351, -0.127758, 1)

            if (addresses != null) {
                addresses[0].countryCode
            } else {
                "No Country"
            }
        } catch (e: Exception) {
            "Error getting country"
        }
    }

    // Get currency from country
    private fun getCurrencyFromCountry(country: String): String {
        var currency_code : String
        var currency_code_list = ArrayList<String>()
        currency_code_list.addAll(listOf("USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "NZD"))

        try {
            currency_code = Currency.getInstance(Locale("en", country)).currencyCode
            if (!currency_code_list.contains(currency_code)) {
                Log.i("Not supported country", "unsupported country")
                currency_code = "USD"
            }

        } catch (e: Exception) {
            Log.e("Exception getting country", e.message ?: "No message provided")
            currency_code = "USD"
        }
        return currency_code
    }

    // Update currency to Firebase database
    private fun updateCurrencyToDb(currency: String){
        UserRepository.update(CurrentUser.userId!!, currency = currency)
        CurrentUser.currency = currency
    }


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 200
    }
}
