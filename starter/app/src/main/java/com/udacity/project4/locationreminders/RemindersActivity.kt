package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_INDEX = 0
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val GEOFENCE_RADIUS_IN_METERS = 50f
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var binding: ActivityRemindersBinding
    private val TAG = this::class.java.simpleName
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val viewModel by viewModel<RemindersListViewModel>()
    private val loginViewModel by viewModel<LoginViewModel>()
    lateinit var navController: NavController
    private val snackBar by lazy {
        Snackbar.make(
            binding.root,
            "text", Snackbar.LENGTH_INDEFINITE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityRemindersBinding>(
            this,
            R.layout.activity_reminders
        )
        geofencingClient = LocationServices.getGeofencingClient(this)
        viewModel.loadReminders()
        navController = findNavController(R.id.navHostFragment)


    }

    fun hideSnackBar() {
        snackBar.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.navHostFragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (loginViewModel.authenticationState.value == AuthenticationState.AUTHENTICATED)
            checkOnLocation()


    }

    private fun checkOnLocation() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            checkDeviceLocationSettings(context = this)
    }


    /*
    *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
    */
    @TargetApi(29)
    fun requestForegroundAndBackgroundLocationPermissions(foregroundOnly: Boolean = false) {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater && !foregroundOnly -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this,
            permissionsArray,
            resultCode
        )
    }

    fun checkDeviceLocationSettings(
        context: AppCompatActivity = this,
        resolve: Boolean = true,
        success: Runnable? = null,
        failure: Runnable? = null
    ) {
        if (get(named("isTesting"))) {
            success?.run()
            return
        }
        if (!viewModel.remindersList.value.isNullOrEmpty()) return
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(context)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            failure?.run()
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        context,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                snackBar.setText(R.string.location_required_error)
                    .setAction(android.R.string.ok) {
                        checkDeviceLocationSettings(context = this)
                    }.show()

            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForClue(viewModel.remindersList.value ?: emptyList())
                success?.run()
            }
        }
    }

    @TargetApi(29)
    fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        if (get(named("isTesting"))) return true
        val foregroundLocationApproved = checkOnForegroundLocation()
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    fun checkOnForegroundLocation() =
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettings(this, false)
        }
    }

    /*
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")
        if (grantResults.isEmpty() && requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) return
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // Permission denied.
            snackBar.setText(R.string.permission_denied_explanation)
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            //you have Permission
        }
    }


    /*
    * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
    * method should be called after the user has granted the location permission.  If there are
    * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
    * is now "active."
    */
    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue(reminders: List<ReminderDataItem>) {
        if (!foregroundAndBackgroundLocationPermissionApproved()) return
        geofencingClient.removeGeofences(reminders.map { it.id })?.run {
            addOnCompleteListener {
                val intent = Intent(this@RemindersActivity, GeofenceBroadcastReceiver::class.java)

                intent.action = ACTION_GEOFENCE_EVENT
                // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
                // addGeofences() and removeGeofences().
                val pendingIntent = PendingIntent.getBroadcast(
                    this@RemindersActivity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                // Add the geofences to be monitored by geofencing service.
                // Build the Geofence Object
                val geofencingRequest = GeofencingRequest.Builder()
                    // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
                    // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
                    // is already inside that geofence.
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
                for (currentGeofenceData in reminders) {
                    if (currentGeofenceData.latitude == null || currentGeofenceData.longitude == null)
                        continue


                    val geofence = Geofence.Builder()
                        // Set the request ID, string to identify the geofence.
                        .setRequestId(currentGeofenceData.id)
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                            currentGeofenceData.latitude!!,
                            currentGeofenceData.longitude!!,
                            GEOFENCE_RADIUS_IN_METERS
                        )
                        // Set the expiration duration of the geofence. This geofence gets
                        // automatically removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()

                    // Build the geofence request

                    geofencingRequest.addGeofence(geofence)


                }
                geofencingClient.addGeofences(geofencingRequest.build(), pendingIntent)


            }
        }


    }

    @SuppressLint("MissingPermission")
    fun addGeofenceForClue(reminder: ReminderDataItem) {
        if (!foregroundAndBackgroundLocationPermissionApproved()) return
        if (reminder.latitude == null || reminder.longitude == null)
            return
        val intent = Intent(this@RemindersActivity, GeofenceBroadcastReceiver::class.java)

        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        val pendingIntent = PendingIntent.getBroadcast(
            this@RemindersActivity,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Add the geofences to be monitored by geofencing service.
        // Build the Geofence Object
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)


        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(reminder.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                reminder.latitude!!,
                reminder.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request

        geofencingRequest.addGeofence(geofence)
        geofencingClient.addGeofences(geofencingRequest.build(), pendingIntent)
    }

    fun removeGeoFence() {
        val reminders = viewModel.remindersList.value ?: emptyList()
        if (reminders.isNotEmpty())
            geofencingClient.removeGeofences(reminders.map { it.id })
    }


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "com.udacity.project4.locationreminders.ACTION_GEOFENCE_EVENT"
    }
}
