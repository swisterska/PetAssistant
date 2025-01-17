package com.example.finalproject

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Calendar

class FoodTimesActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setTimeButton: Button
    private lateinit var timestampsTextView: TextView
    private val timestamps = mutableListOf<Calendar>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_times)


        // Initialize views
        timePicker = findViewById(R.id.timePickerFood)
        setTimeButton = findViewById(R.id.setTimeFoodNotif)
        timestampsTextView = findViewById(R.id.timestampsTextView)

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
            scheduleNotification(calendar)

            /// Update the TextView to show the set timestamps
            updateTimestampsTextView()
        }
    }

    // Method to schedule a notification
    private fun scheduleNotification(calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an Intent to trigger the notification
        val intent = Intent(this, NotificationReceiver::class.java)

        // Use the timestamp to generate a unique request code for each alarm
        val requestCode = calendar.timeInMillis.toInt() // Unique identifier based on the time

        // Create a PendingIntent that will trigger the NotificationReceiver
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm to trigger at the scheduled time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        // Log for debugging purposes
        println("Scheduled Notification at: ${calendar.time}")
    }

    // Method to update the TextView with the list of scheduled timestamps
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
