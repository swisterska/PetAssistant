package com.example.finalproject

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

/**
 * Activity for setting feeding times for pets and scheduling notifications.
 */
class FoodTimesActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setTimeButton: Button
    private lateinit var timestampsTextView: TextView
    private val timestamps = mutableListOf<Calendar>()

    /**
     * Called when the activity is created. Initializes the UI components and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_times)


        // Initialize views
        timePicker = findViewById(R.id.timePickerFood)
        setTimeButton = findViewById(R.id.setTimeFoodNotif)
        timestampsTextView = findViewById(R.id.timestampsTextView)

        // Go back button functionality
        val goBackButton: ImageButton = findViewById(R.id.GoBackButtonFoodTimes)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
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

            timestamps.add(calendar)

            // Show the selected time in a Toast
            val selectedTime = String.format("%02d:%02d", hour, minute)
            Toast.makeText(this, "Time Set: $selectedTime", Toast.LENGTH_SHORT).show()

            // Schedule the alarm for the selected timestamp
            scheduleNotification(calendar, "Food")

            /// Update the TextView to show the set timestamps
            updateTimestampsTextView()
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
     * Updates the TextView with the list of scheduled feeding times.
     */
    private fun updateTimestampsTextView() {
        // Format the timestamps list as a string
        val timestampsList = timestamps.joinToString("\n") {
            val hour = it.get(Calendar.HOUR_OF_DAY)
            val minute = it.get(Calendar.MINUTE)
            String.format("%02d:%02d", hour, minute)
        }

        // Update the TextView with the formatted string
        timestampsTextView.text = "Scheduled Times:\n$timestampsList"
    }
}
