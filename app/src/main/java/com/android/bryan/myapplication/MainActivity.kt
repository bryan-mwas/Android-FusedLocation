package com.android.bryan.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    /**
     * Provides entry to Fused Location Provider API.
     */
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    /**
     * Represents geographical location
     */
    private var mLastLocation: Location? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private var mLatitudeText: TextView? = null
    private var mLongitudeText: TextView? = null

    companion object {
        private val TAG = "LocationProvider"
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLatitudeLabel = resources.getString(R.string.latitude_label)
        mLongitudeLabel = resources.getString(R.string.longitude_label)

        mLatitudeText = findViewById<View>(R.id.latitude_text) as TextView
        mLongitudeText = findViewById<View>(R.id.longitude_text) as TextView

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationProviderClient!!.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                mLastLocation = task.result
                mLatitudeText!!.setText("$mLatitudeLabel: (${mLastLocation!!.latitude})")
                mLongitudeText!!.setText("$mLongitudeLabel: (${mLastLocation!!.longitude})")
            } else {
                Log.w("MainActivity", "getLastLocation:exception", task.exception)
                showMessage(getString(R.string.no_location_detected))
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    /**
     * Shows a [] using `text`.

     * @param text The Snackbar text.
     */
    private fun showMessage(text: String) {
        val container = findViewById<View>(R.id.main_activity_container)
        if (container != null) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shows a [].

     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * *
     * @param actionStringId   The text of the action item.
     * *
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {

        Toast.makeText(this@MainActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }


    private fun requestPermissions() {
        val shouldProvideAdditionalRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        // Provide additional rational to the user. This would happen if the user denied the request previously,
        // but didn't check the "Don't ask me again" checkbox.
        if (shouldProvideAdditionalRationale) {
            Log.i("MainActivity", "Displaying permission rationale to provide additional context")
            showSnackbar(R.string.permission_rationale, android.R.string.ok, View.OnClickListener {
                // Request permission
                startLocationPermissionRequest()
            })
        } else {
            Log.i("MainActivity", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy sets the permission
            // in a given state or the user denied the permission previously and checked "Never ask again"
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i("MainActivity", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If the user interaction was interrupted, the permission is cancelled and you receive empty arrays
                Log.i("MainActivity", "User interaction was cancelled")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLastLocation()
            } else {
                // Permission denied
                // Notify the user via a SnackBar that they have rejected a core permission for the app, which makes
                // the activity useless. In a real app, core permission would typically be best requested during a
                // welcome-screen flow
                // Additionally, it is important to remember that a permission might have been rejected without asking
                // the user for permission device policy or "Never ask again" prompts. Thus, a user interface affordance
                // is typically implemented when the permission are denied. Otherwise, the app could appear unresponsive
                // to touches or interactions which have required permissions
                showSnackbar(R.string.permission_denied_explanation, R.string.settings, View.OnClickListener {
                    // Build intent that displays App settings screen
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                })
            }
        }
    }
}
