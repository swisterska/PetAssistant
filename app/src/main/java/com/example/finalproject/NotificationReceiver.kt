package com.example.finalproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        // Step 1: Configure the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Pet Assistant")
            .setContentText("Give food to your pet")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Step 2: Create the notification channel (required for Android 8.0 and above)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Food Reminder Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        NotificationManagerCompat.from(context).createNotificationChannel(channel)

        // Step 3: Obtain the NotificationManagerCompat instance
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

        // Step 4: Check notification permissions (Android 13 and above)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // If permissions are not granted, exit without sending the notification
            return
        }
        // Step 5: Send the notification
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * Companion object to hold constant values for the NotificationChannel and Notification ID.
     *
     * ### Why use a companion object?
     * Companion objects in Kotlin are used to define static members that belong to the class rather than a specific instance.
     */
    companion object {
        private const val CHANNEL_ID = "FoodReminderChannel"
        private const val NOTIFICATION_ID = 1
    }
}
