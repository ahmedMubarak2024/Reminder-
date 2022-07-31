package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
            )
            else -> resources.getString(R.string.unknown_geofence_error)
        }
    }

    private val dataSource: ReminderDataSource by inject()
    val TAG = this::class.java.simpleName
    val scope = CoroutineScope(get(named("MAIN")))
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            val errorMessage = errorMessage(context, geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        if (geofencingEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.v(TAG, context.getString(R.string.geofence_entered))


            sendNotification(geofencingEvent.triggeringGeofences?.map { it.requestId }
                ?: emptyList(), context)
        }


    }

    private fun sendNotification(triggeringGeofences: List<String>, context: Context) {
        Log.i(TAG, "sendNotification: " + triggeringGeofences.toString())
        scope.launch {
            triggeringGeofences.forEach {
                val reminder = getReminder(it)
                if (reminder is Result.Success) {
                    val data = reminder.data
                    com.udacity.project4.utils.sendNotification(
                        context, ReminderDataItem(
                            data.title,
                            data.description,
                            data.location,
                            data.latitude,
                            data.longitude,
                            data.id
                        )
                    )

                }
            }
        }

        //send a notification to the user with the reminder details

    }

    suspend fun getReminder(id: String) =
        withContext(get(named("IO"))) { dataSource.getReminder(id) }


}

