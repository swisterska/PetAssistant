package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

/**
 * SymptomsAddActivity is the activity where users can add symptoms for their pets.
 * The input from the user will be saved to Firebase Realtime Database under a specific pet,
 * along with the current timestamp.
 */
class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private lateinit var petId: String

    /**
     * Called when the activity is created.
     * Initializes the UI components, sets up listeners, and retrieves the userId and petId
     * from the Intent to save symptoms under a specific pet.
     *
     * @param savedInstanceState The saved instance state for the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_add)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Initialize UI components
        symptomInput = findViewById(R.id.symptomInput)
        addSymptomButton = findViewById(R.id.addSymptom)

        // Retrieve userId and petId from the Intent
        userId = intent.getStringExtra("userId") ?: return
        petId = intent.getStringExtra("petId") ?: return

        // Go back button functionality
        val goBackButtonS: ImageButton = findViewById(R.id.GoBackButtonSymptoms)
        goBackButtonS.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        // Set up add symptom button functionality
        addSymptomButton.setOnClickListener {
            saveSymptomToFirebase()
        }
    }

    /**
     * Saves the entered symptom to Firebase Realtime Database under the specific pet of a user.
     * The symptom is stored under the "symptoms" node of the selected pet and is tagged with a timestamp.
     *
     * @throws IllegalArgumentException if userId or petId is not provided or is invalid.
     */
    private fun saveSymptomToFirebase() {
        // Get input from EditText
        val symptom = symptomInput.text.toString().trim()

        // Validate the input
        if (TextUtils.isEmpty(symptom)) {
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unique key for the symptom
        val symptomId = databaseReference.child(userId).child("pets").child(petId).child("symptoms").push().key

        // Create a map to hold symptom data
        val symptomData = mapOf(
            "id" to symptomId,
            "symptom" to symptom,
            "date" to ServerValue.TIMESTAMP // Use Firebase's server timestamp for accurate date and time
        )

        // Check if symptomId is generated
        symptomId?.let {
            // Save the symptom data under the specific pet and user
            databaseReference.child(userId).child("pets").child(petId).child("symptoms").child(it).setValue(symptomData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                        symptomInput.text.clear() // Clear the input field
                    } else {
                        Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            // Handle case where symptomId is null
            Toast.makeText(this, "Error generating symptom ID", Toast.LENGTH_SHORT).show()
        }
    }
}
