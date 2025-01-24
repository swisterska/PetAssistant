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
 * NotificationReceiver handles notifications for both food and water reminders.
 */
class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        // Extract the reminder type (food or water) from the intent
        val reminderType = intent.getStringExtra("REMINDER_TYPE") ?: "Food"

        // Set notification content based on the reminder type
        val notificationTitle = "Pet Assistant Reminder"
        val notificationText = when (reminderType) {
            "Water" -> "Time to change your pet's water!"
            else -> "Time to feed your pet!"
        }

        // Create the notification channel
        createNotificationChannel(context)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Obtain the NotificationManagerCompat instance
        val notificationManager = NotificationManagerCompat.from(context)

        // Check notification permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Send the notification with a unique ID
        val uniqueNotificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
    }

    companion object {
        private const val CHANNEL_ID = "PetReminderChannel"
    }

    /**
     * Creates a notification channel if necessary.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pet Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
