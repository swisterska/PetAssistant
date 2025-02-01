package com.example.finalproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * NHandles notifications for both food and water reminders.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Called when the alarm triggers, retrieves the pet's name, and displays a notification.
     *
     * @param context The application context.
     * @param intent The received intent containing reminder details.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val reminderType = intent.getStringExtra("REMINDER_TYPE") ?: "Food"
        val petId = intent.getStringExtra("petId") // Retrieve petId

        if (petId != null) {
            fetchPetName(context, petId, reminderType)
        } else {
            Log.e("NotificationReceiver", "Pet ID is null. Cannot fetch pet name.")
        }
    }

    /**
     * Fetches the pet's name from Firestore and triggers a notification.
     *
     * @param context The application context.
     * @param petId The unique ID of the pet.
     * @param reminderType The type of reminder ("Food" or "Water").
     */
    private fun fetchPetName(context: Context, petId: String, reminderType: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection("users").document(userId).collection("pets").document(petId)

        petRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val petName = document.getString("name") ?: "Your pet"
                showNotification(context, petName, reminderType)
            } else {
                Log.e("NotificationReceiver", "Pet not found in Firestore")
            }
        }.addOnFailureListener { exception ->
            Log.e("NotificationReceiver", "Error fetching pet name: ${exception.message}")
        }
    }

    /**
     * Displays a notification reminding the user to feed or change their pet’s water.
     *
     * @param context The application context.
     * @param petName The pet's name.
     * @param reminderType The type of reminder ("Food" or "Water").
     */
    private fun showNotification(context: Context, petName: String, reminderType: String) {
        createNotificationChannel(context)

        val notificationTitle = "Pet Assistant Reminder"
        val notificationText = when (reminderType) {
            "Water" -> "Time to change $petName's water!"
            else -> "Time to feed $petName!"
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val uniqueNotificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
    }

    companion object {
        private const val CHANNEL_ID = "PetReminderChannel"
    }

    /**
     * Creates a notification channel for pet reminders (required for Android 8.0+).
     *
     * @param context The application context.
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