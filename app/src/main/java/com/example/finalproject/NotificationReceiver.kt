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
            // Create the notification channel
            createNotificationChannel(context)

            // Configure the notification
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Pet Assistant")
                .setContentText("Give food to your pet")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Obtain the NotificationManagerCompat instance
            val notificationManager: NotificationManagerCompat =
                NotificationManagerCompat.from(context)

            // Check notification permissions
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                // If permissions are not granted, exit without sending the notification
                return
            }

            // Send the notification with a unique ID
            val uniqueNotificationId = System.currentTimeMillis().toInt() // Generate a unique ID
            notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
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
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Food Reminder Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
