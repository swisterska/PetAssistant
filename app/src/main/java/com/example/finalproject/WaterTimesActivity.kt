package com.example.finalproject

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity that allows users to set and manage water change times for their pets.
 * It supports scheduling notifications for reminders and storing water change times in Firestore.
 */
class WaterTimesActivity : AppCompatActivity() {

    private lateinit var timePickerW: TimePicker
    private lateinit var setTimeButtonW: Button
    private lateinit var clearTimeButtonW: ImageButton
    private lateinit var timestampsTextViewW: TextView
    private val timestampsW = mutableListOf<String>()

    /**
     * Called when the activity is created. This method initializes the UI components and sets up event listeners.
     * It also loads existing water change times from Firestore if a pet ID is passed in the intent.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_times)

        val petId = intent.getStringExtra("petId")

        // Initialize views
        timePickerW = findViewById(R.id.timePickerWater)
        setTimeButtonW = findViewById(R.id.setTimeWaterNotif)
        clearTimeButtonW = findViewById(R.id.clearTimeWaterNotif)
        timestampsTextViewW = findViewById(R.id.timestampsTextViewW)

        // Go back button functionality
        val goBackButton: ImageButton = findViewById(R.id.GoBackButtonWaterTimes)
        goBackButton.setOnClickListener {

            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        // Set time button functionality to store and schedule a water change reminder
        setTimeButtonW.setOnClickListener {
            // Get the selected time from the TimePicker
            val hour = timePickerW.hour // API 23 and above (for API 23+)
            val minute = timePickerW.minute

            // Store the selected time as a timestamp
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // Format the time to string (HH:mm format)
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            timestampsW.add(timeString)

            // Show the selected time in a Toast
            val selectedTime = String.format("%02d:%02d", hour, minute)
            Toast.makeText(this, "Time Set: $selectedTime", Toast.LENGTH_SHORT).show()

            // Schedule the alarm for the selected timestamp
            scheduleNotification(calendar, "Water")

            /// Update the TextView to show the set timestamps
            updateTimestampsTextView()

            // Save the water change time to Firestore
            if (petId != null) {
                saveWaterChangeTimeToFirestore(petId, timeString)
            }
        }

        // Clear time button functionality to delete all water change times from Firestore
        clearTimeButtonW.setOnClickListener {
            if (petId != null) {
                deleteAllWaterTimesFromFirestore(petId)
            }
        }

        // Load the existing water times from Firestore if the user is logged in
        if (petId != null) {
            loadWaterTimesFromFirestore(petId)
        }
    }

    /**
     * Schedules a notification to trigger at the specified time for the water change reminder.
     *
     * @param calendar The Calendar object representing the scheduled time for the reminder.
     * @param reminderType The type of reminder (in this case, "Water").
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun scheduleNotification(calendar: Calendar, reminderType: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", reminderType)
            val petId = intent.getStringExtra("petId")
            putExtra("petId", petId)
        }

        val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        println("Scheduled $reminderType Notification at: ${calendar.time}")
    }

    /**
     * Saves the selected water change time to Firestore under the current user's pet.
     *
     * @param petID The ID of the pet for which the time is being set.
     * @param timeString The formatted time string for the water change reminder.
     */
    private fun saveWaterChangeTimeToFirestore(petID: String, timeString: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)

        // Update the water change time list in Firestore
        dbRef.update("waterChangeTime", FieldValue.arrayUnion(timeString))
            .addOnSuccessListener {
                Log.d("Firestore", "Water change time added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding water change time", e)
            }
    }

    /**
     * Loads the existing water change times from Firestore for a specified pet.
     *
     * @param petID The ID of the pet whose water change times are being retrieved.
     */
    private fun loadWaterTimesFromFirestore(petID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)

        dbRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val waterTimes = document.get("waterChangeTime") as? List<String> ?: emptyList()

                    // Convert Firestore Strings to Calendar objects
                    waterTimes.forEach { timeString ->
                        val calendar = Calendar.getInstance()
                        try {
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeString)
                            calendar.time = time
                        } catch (e: Exception) {
                            Log.e("FoodTimesActivity", "Error parsing feeding time string", e)
                        }
                        timestampsW.add(timeString)
                    }

                    // Update the TextView to show the retrieved timestamps
                    updateTimestampsTextView()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting water change times", e)
            }
    }

    /**
     * Updates the TextView with the list of scheduled water change times.
     */
    private fun updateTimestampsTextView() {
        // Format the timestamps list as a string
        val timestampsList = timestampsW.joinToString("\n")

        // Update the TextView with the formatted string
        timestampsTextViewW.text = if (timestampsList.isEmpty()) {
            "No water times set."
        } else {
            "$timestampsList"
        }
    }

    /**
     * Deletes all water change times for a specified pet from Firestore.
     *
     * @param petID The ID of the pet for which all water change times should be deleted.
     */
    private fun deleteAllWaterTimesFromFirestore(petID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)

        // Set the feeding time list to an empty list in Firestore
        dbRef.update("waterChangeTime", FieldValue.arrayRemove(*timestampsW.toTypedArray()))
            .addOnSuccessListener {
                Log.d("Firestore", "All water times deleted successfully for pet $petID")
                // Clear the list of timestamps and update the UI
                timestampsW.clear()
                updateTimestampsTextView()

                // Show a Toast indicating that the times were deleted
                Toast.makeText(this, "All water times deleted", Toast.LENGTH_SHORT).show()

                // Optionally reload feeding times to ensure list is empty
                loadWaterTimesFromFirestore(petID)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting water times for pet $petID", e)
            }
    }

}

