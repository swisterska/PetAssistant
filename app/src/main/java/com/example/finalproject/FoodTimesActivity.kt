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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for setting feeding times for pets and scheduling notifications.
 */
class FoodTimesActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setTimeButton: Button
    private lateinit var clearTimeButton: Button
    private lateinit var timestampsTextView: TextView
    private val timestamps = mutableListOf<String>()

    /**
     * Called when the activity is created. Initializes the UI components and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_times)

        val petId = intent.getStringExtra("petId")

        // Initialize views
        timePicker = findViewById(R.id.timePickerFood)
        setTimeButton = findViewById(R.id.setTimeFoodNotif)
        clearTimeButton = findViewById(R.id.clearTimeFoodNotif)
        timestampsTextView = findViewById(R.id.timestampsTextView)

        // Go back button functionality
        val goBackButton: ImageButton = findViewById(R.id.GoBackButtonFoodTimes)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        setTimeButton.setOnClickListener {
            // Get the selected time from the TimePicker
            val hour = timePicker.hour // API 23 and above (for API 23+)
            val minute = timePicker.minute

            // Store the selected time as a timestamp
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // Format the time to string (HH:mm format)
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
            timestamps.add(timeString)

            // Show the selected time in a Toast
            val selectedTime = String.format("%02d:%02d", hour, minute)
            Toast.makeText(this, "Time Set: $selectedTime", Toast.LENGTH_SHORT).show()

            // Schedule the alarm for the selected timestamp
            scheduleNotification(calendar, "Food")

            /// Update the TextView to show the set timestamps
            updateTimestampsTextView()

            // Save the feeding time to Firestore
            if (petId != null) {
                saveFeedingTimeToFirestore(petId, timeString)
            }
        }

        clearTimeButton.setOnClickListener {
            // Call the function to delete all feeding times
            if (petId != null) {
                deleteAllFeedingTimesFromFirestore(petId)
            }
        }

        // Load the existing feeding times from Firestore if the user is logged in
        if (petId != null) {
            loadFeedingTimesFromFirestore(petId)
        }

    }
    /**
     * Schedules a notification to trigger at the specified time.
     *
     * @param calendar The Calendar object representing the scheduled time.
     * @param reminderType The type of notification
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
    * Saves the selected feeding time to Firestore under the current user's pet.
    */
    private fun saveFeedingTimeToFirestore(petID: String, timeString: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)  // Use the petID passed to this function

        // Update the feeding time list in Firestore (store as String)
        dbRef.update("feedingTime", FieldValue.arrayUnion(timeString))
            .addOnSuccessListener {
                Log.d("Firestore", "Feeding time added successfully for pet $petID")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding feeding time for pet $petID", e)
            }
    }


    private fun loadFeedingTimesFromFirestore(petID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)  // Use the petID passed to the function

        dbRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val feedingTimes = document.get("feedingTime") as? List<String> ?: emptyList()

                    // Convert Firestore Strings to Calendar objects
                    feedingTimes.forEach { timeString ->
                        val calendar = Calendar.getInstance()
                        try {
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeString)
                            calendar.time = time
                        } catch (e: Exception) {
                            Log.e("FoodTimesActivity", "Error parsing feeding time string", e)
                        }
                        timestamps.add(timeString)
                    }

                    // Update the TextView to show the retrieved timestamps
                    updateTimestampsTextView()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting feeding times", e)
            }
    }



    /**
     * Updates the TextView with the list of scheduled feeding times.
     */
    private fun updateTimestampsTextView() {
        // Format the timestamps list as a string
        val timestampsList = timestamps.joinToString("\n")

        // Update the TextView with the formatted string
        timestampsTextView.text = if (timestampsList.isEmpty()) {
            "No feeding times set."
        } else {
            "Scheduled Times:\n$timestampsList"
        }
    }


    private fun deleteAllFeedingTimesFromFirestore(petID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)  // Use the petID passed to this function

        // Set the feeding time list to an empty list in Firestore
        dbRef.update("feedingTime", FieldValue.arrayRemove(*timestamps.toTypedArray()))
            .addOnSuccessListener {
                Log.d("Firestore", "All feeding times deleted successfully for pet $petID")
                // Clear the list of timestamps and update the UI
                timestamps.clear()
                updateTimestampsTextView()

                // Show a Toast indicating that the times were deleted
                Toast.makeText(this, "All feeding times deleted", Toast.LENGTH_SHORT).show()

                // Optionally reload feeding times to ensure list is empty
                loadFeedingTimesFromFirestore(petID)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting feeding times for pet $petID", e)
            }
    }


}
