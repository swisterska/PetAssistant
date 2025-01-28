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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * SymptomsAddActivity is the activity where users can add symptoms for their pets.
 * The input from the user will be saved to Firebase Firestore under a specific pet,
 * along with the current timestamp.
 */
class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var db: FirebaseFirestore

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
            intent.putExtra("petId", petId)
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        // Set up add symptom button functionality
        addSymptomButton.setOnClickListener {
            Log.d("SymptomsAddActivity", "AddSymptomButton clicked")
            if (petId != null) {
                saveSymptomToFirestore(petId)
            }
        }
    }

    /**
     * Saves the entered symptom to Firestore under the specific pet of a user.
     * The symptom is stored under the "symptoms" subcollection of the selected pet and is tagged with a timestamp.
     *
     * @throws IllegalArgumentException if userId or petId is not provided or is invalid.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveSymptomToFirestore(petID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)
            .collection("symptoms") // Storing symptoms in a subcollection

        // Get input from EditText (the symptom text)
        val symptomText = symptomInput.text.toString().trim()
        Log.d("SymptomsAddActivity", "Symptom input: $symptomText")

        // Validate the input
        if (TextUtils.isEmpty(symptomText)) {
            Log.d("SymptomsAddActivity", "Symptom input is empty")
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current timestamp
        val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

        // Generate a new document reference to get Firestore auto-generated ID
        val newSymptomRef = dbRef.document()

        // Create a SymptomData object
        val symptomData = SymptomData(
            id = newSymptomRef.id,  // Auto-generated Firestore ID
            symptom = symptomText,
            timestamp = timeString
        )

        // Add new symptom as a document in the "symptoms" subcollection
        dbRef.add(symptomData)
            .addOnSuccessListener {
                Log.d("SymptomsAddActivity", "Symptom added successfully for pet $petID")
                Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                symptomInput.text.clear()  // Clear input field
            }
            .addOnFailureListener { e ->
                Log.e("SymptomsAddActivity", "Error adding symptom for pet $petID", e)
                Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
            }
    }
}

