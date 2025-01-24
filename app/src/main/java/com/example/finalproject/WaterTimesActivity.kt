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
import java.util.Calendar

/**
 * Activity for setting water change times for pets and scheduling notifications.
 */
class WaterTimesActivity : AppCompatActivity() {

    private lateinit var timePickerW: TimePicker
    private lateinit var setTimeButtonW: Button
    private lateinit var timestampsTextViewW: TextView
    private val timestampsW = mutableListOf<Calendar>()

    /**
     * Called when the activity is created. Initializes the UI components and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_times)


        // Initialize views
        timePickerW = findViewById(R.id.timePickerWater)
        setTimeButtonW = findViewById(R.id.setTimeWaterNotif)
        timestampsTextViewW = findViewById(R.id.timestampsTextViewW)

        // Go back button functionality
        val goBackButton: ImageButton = findViewById(R.id.GoBackButtonWaterTimes)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

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

            timestampsW.add(calendar)

            // Show the selected time in a Toast
            val selectedTime = String.format("%02d:%02d", hour, minute)
            Toast.makeText(this, "Time Set: $selectedTime", Toast.LENGTH_SHORT).show()

            // Schedule the alarm for the selected timestamp
            scheduleNotification(calendar, "Water")

            /// Update the TextView to show the set timestamps
            updateTimestampsTextView()
        }
    }
    /**
     * Schedules a notification to trigger at the specified time.
     *
     * @param calendar The Calendar object representing the scheduled time.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun scheduleNotification(calendar: Calendar, reminderType: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", reminderType) // Pass either "Food" or "Water"
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
     */
    private fun saveWaterChangeTimeToFirestore(calendar: Calendar) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")  // assuming the pet data is saved here
            .document("petId")  // Replace with actual pet ID

        // Convert the calendar to a Timestamp for Firestore
        val waterChangeTime = Timestamp(calendar.time)

        // Update the water change time list in Firestore
        dbRef.update("waterChangeTime", FieldValue.arrayUnion(waterChangeTime))
            .addOnSuccessListener {
                Log.d("Firestore", "Water change time added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding water change time", e)
            }
    }

    /**
     * Updates the TextView with the list of scheduled water change times.
     */
    private fun updateTimestampsTextView() {
        // Format the timestamps list as a string
        val timestampsList = timestampsW.joinToString("\n") {
            val hour = it.get(Calendar.HOUR_OF_DAY)
            val minute = it.get(Calendar.MINUTE)
            String.format("%02d:%02d", hour, minute)
        }

        // Update the TextView with the formatted string
        timestampsTextViewW.text = "Scheduled Times:\n$timestampsList"
    }
}
