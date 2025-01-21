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

/**
 * NotificationReceiver is a BroadcastReceiver responsible for handling notifications.
 * It listens for broadcast intents and sends a notification to the user when triggered.
 * The notification reminds the user to give food to their pet.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * This method is called when the receiver receives a broadcast.
     * It creates a notification channel (if necessary) and builds a notification.
     * The notification is then sent to the user.
     *
     * @param context the context in which the receiver is running
     * @param intent the intent that triggered this receiver
     */
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
     * Companion object to hold constant values related to the NotificationChannel.
     * It includes the channel ID and notification ID.
     */
    companion object {
        private const val CHANNEL_ID = "FoodReminderChannel"
        private const val NOTIFICATION_ID = 1
    }

    /**
     * Creates a notification channel for devices running Android Oreo (API 26) and above.
     * The channel is used to categorize the notification and manage its behavior.
     *
     * @param context the context in which the notification channel will be created
     */
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
