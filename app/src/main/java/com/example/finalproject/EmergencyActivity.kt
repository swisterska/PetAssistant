package com.example.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    private val vetPhoneNumber = "606546183"

    private var petName: String = "Unknown Pet"
    private var userName: String = "Unknown User"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    /**
     * Called when the activity is created. This method initializes the UI components,
     * starts the countdown timer from 5 seconds, and sets up the stop button functionality.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        val petId = intent.getStringExtra("petId")
        
        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        // Initialize UI components
        countdownTimerText = findViewById(R.id.countdownTimer)
        emergencyMessageText = findViewById(R.id.emergencyMessage)
        stopButton = findViewById(R.id.stopButton)


        // Request SMS permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        // Fetch pet and user details BEFORE starting the countdown
        fetchUserAndPetData {
            startCountdown()
        }

        stopButton.setOnClickListener {
            isStopped = true
            Toast.makeText(this, "Countdown stopped", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Fetches user and pet data and executes the callback once done.
     */
    private fun fetchUserAndPetData(onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { userDocument ->
            if (userDocument.exists()) {
                userName = userDocument.getString("username") ?: "Unknown User"

                val petId = intent.getStringExtra("petId") ?: return@addOnSuccessListener

                val petRef = userRef.collection("pets").document(petId)
                petRef.get().addOnSuccessListener { petDocument ->
                    if (petDocument.exists()) {
                        petName = petDocument.getString("name") ?: "Unknown Pet"

                        // Data fetching complete, execute callback
                        onComplete()
                    } else {
                        Log.e("Firestore", "Pet not found for user $userName")
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching pet data: ${e.message}")
                }
            } else {
                Log.e("Firestore", "User not found in Firestore")
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching user data: ${e.message}")
        }
    }

    /**
     * Starts the countdown timer.
     */
    private fun startCountdown() {
        val countdownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isStopped) {
                    cancel()
                } else {
                    countdownTimerText.text = (millisUntilFinished / 1000).toString()
                }
            }

            override fun onFinish() {
                if (!isStopped) {
                    sendSmsToVet()
                }
            }
        }
        countdownTimer.start()
    }

    /**
     * Sends an SMS to the vet when the countdown finishes.
     */
    private fun sendSmsToVet() {
        val smsMessage = "Pet $petName owned by $userName has an emergency. They will likely visit you soon."
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(vetPhoneNumber, null, smsMessage, null, null)
            Toast.makeText(this, "SMS sent to vet.", Toast.LENGTH_LONG).show()

            emergencyMessageText.text = "A message has been sent to the nearby vet."
            emergencyMessageText.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}