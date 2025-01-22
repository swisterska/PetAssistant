package com.example.finalproject

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Date
import java.util.Locale

/**
 * SymptomsAddActivity is the activity where users can add a symptom.
 * The input from the user will be saved to Firebase Realtime Database with the current date and time.
 */
class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var databaseReference: DatabaseReference

    /**
     * onCreate is the entry point for this activity.
     * It initializes the UI components and sets up listeners.
     *
     * @param savedInstanceState the state of the activity instance during its creation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_add)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("symptoms")

        // Link UI components
        symptomInput = findViewById(R.id.symptomInput)
        addSymptomButton = findViewById(R.id.addSymptom)

        // Go back button functionality
        val goBackButton: ImageButton = findViewById(R.id.GoBackButtonSymptoms)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        // Set up add symptom button functionality
        addSymptomButton.setOnClickListener {
            saveSymptomToFirebase()
        }
    }

    /**
     * saveSymptomToFirebase is responsible for saving the symptom data
     * entered by the user to Firebase Realtime Database, along with the current timestamp.
     *
     * This method ensures that:
     * - The user input is not empty.
     * - The symptom is saved under a unique ID.
     * - The current date and time are recorded along with the symptom data.
     */
    private fun saveSymptomToFirebase() {
        // Get input from EditText
        val symptom = symptomInput.text.toString().trim()

        if (TextUtils.isEmpty(symptom)) {
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current date and time
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Create a unique key for each symptom
        val symptomId = databaseReference.push().key

        // Create a map for the data
        val symptomData = mapOf(
            "id" to symptomId,
            "symptom" to symptom,
            "date" to currentDate
        )

        // Save to Firebase
        symptomId?.let {
            databaseReference.child(it).setValue(symptomData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                        symptomInput.text.clear() // Clear the input field
                    } else {
                        Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
