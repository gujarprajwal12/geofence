package com.example.geofancing.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.widget.Toast

//class GeofenceBroadcastReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent != null && GeofencingEvent.fromIntent(intent) != null) {
//            val geofencingEvent = GeofencingEvent.fromIntent(intent)
//            if (geofencingEvent!!.hasError()) {
//                Log.e("Geofence", "Geofencing error: ${geofencingEvent.errorCode}")
//                return
//            }
//
//            when (geofencingEvent.geofenceTransition) {
//                Geofence.GEOFENCE_TRANSITION_ENTER -> Log.d("Geofence", "Entered Geofence")
//                Geofence.GEOFENCE_TRANSITION_EXIT -> Log.d("Geofence", "Exited Geofence")
//                else -> Log.d("Geofence", "Unknown transition")
//            }
//        }
//    }
//}



class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent!!.hasError()) {
            Log.e("Geofence", "Error: ${geofencingEvent.errorCode}")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val transitionType = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered Geofence"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited Geofence"
            else -> "Unknown Transition"
        }

        Toast.makeText(context, transitionType, Toast.LENGTH_LONG).show()
        sendNotification(context, transitionType)
    }

    private fun sendNotification(context: Context, message: String) {
        val channelId = "geofence_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create Notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
