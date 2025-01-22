package com.example.finalproject

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * EmergencyActivity is responsible for managing an emergency countdown.
 * If the countdown reaches 0 without being stopped, it simulates sending
 * a notification to a nearby vet.
 */
class EmergencyActivity : AppCompatActivity() {
    private lateinit var countdownTimerText: TextView
    private lateinit var emergencyMessageText: TextView
    private lateinit var stopButton: Button
    private var isStopped = false

    /**
     * Called when the activity is created. This method initializes the UI components,
     * starts the countdown timer from 5 seconds, and sets up the stop button functionality.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        // Initialize UI components
        countdownTimerText = findViewById(R.id.countdownTimer)
        emergencyMessageText = findViewById(R.id.emergencyMessage)
        stopButton = findViewById(R.id.stopButton)

        // Start countdown from 5 seconds
        val countdownTimer = object : CountDownTimer(5000, 1000) {
            /**
             * Called every second to update the countdown timer.
             * If the countdown is stopped by the user, it cancels the timer.
             */
            override fun onTick(millisUntilFinished: Long) {
                if (isStopped) {
                    cancel()  // Stop the countdown if the user presses stop
                } else {
                    countdownTimerText.text = (millisUntilFinished / 1000).toString()
                }
            }
            /**
             * Called when the countdown finishes.
             * If the countdown wasn't stopped, it sends a notification to the vet.
             */
            override fun onFinish() {
                if (!isStopped) {
                    sendNotificationToVet()
                }
            }
        }

        // Start the countdown
        countdownTimer.start()

        // Stop button to cancel the countdown and exit
        stopButton.setOnClickListener {
            isStopped = true
            Toast.makeText(this, "Countdown stopped", Toast.LENGTH_SHORT).show()
            finish() // Exit the activity
        }
    }

    /**
     * sendNotificationToVet simulates sending a message to the nearby vet.
     * This function gets called once the countdown reaches 0, if the user has not stopped it.
     */
    private fun sendNotificationToVet() {
        // Simulate sending a message to the vet
        Toast.makeText(this, "Message sent to nearby vet.", Toast.LENGTH_LONG).show()

        // Display a message to the user indicating the message was sent
        emergencyMessageText.text = "A message has been sent to the nearby vet."
        emergencyMessageText.setTextColor(resources.getColor(android.R.color.holo_green_dark)) // Change text color to green
    }
}