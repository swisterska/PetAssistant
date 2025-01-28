package com.example.finalproject

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * SymptomsAddActivity is the activity where users can add symptoms for their pets.
 * The input from the user will be saved to Firebase Firestore under a specific pet,
 * along with the current timestamp.
 */
class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var petId: String

    /**
     * Called when the activity is created.
     * Initializes the UI components, sets up listeners, and retrieves the userId and petId
     * from the Intent to save symptoms under a specific pet.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_add)

        Log.d("SymptomsAddActivity", "onCreate: Activity created")

        val petId = intent.getStringExtra("petId")

        // Initialize Firestore reference
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        symptomInput = findViewById(R.id.symptomInput)
        addSymptomButton = findViewById(R.id.addSymptom)

        // Go back button functionality
        val returnButton = findViewById<ImageButton>(R.id.GoBackButtonSymptoms)
        returnButton.setOnClickListener {
            Log.d("SymptomsAddActivity", "GoBackButton clicked")
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        // Set up add symptom button functionality
        addSymptomButton.setOnClickListener {
            Log.d("SymptomsAddActivity", "AddSymptomButton clicked")

            saveSymptomToFirestore()
        }
    }

    /**
     * Saves the entered symptom to Firestore under the specific pet of a user.
     * The symptom is stored under the "symptoms" subcollection of the selected pet and is tagged with a timestamp.
     *
     * @throws IllegalArgumentException if userId or petId is not provided or is invalid.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveSymptomToFirestore() {
        // Get input from EditText
        val symptom = symptomInput.text.toString().trim()
        Log.d("SymptomsAddActivity", "Symptom input: $symptom")

        // Validate the input
        if (TextUtils.isEmpty(symptom)) {
            Log.d("SymptomsAddActivity", "Symptom input is empty")
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the symptom data to be saved to Firestore
        val symptomData = hashMapOf(
            "symptom" to symptom,
            "date" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)        )

        // Reference to the specific pet's symptoms subcollection in Firestore
        val symptomRef = db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .collection("symptoms")
            .add(symptomData) // Add a new document to the symptoms collection

        symptomRef.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("SymptomsAddActivity", "Symptom added successfully")
                Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                symptomInput.text.clear() // Clear the input field
            } else {
                Log.d("SymptomsAddActivity", "Failed to add symptom: ${task.exception?.message}")
                Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }
}