package com.example.finalproject

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity that allows users to add and manage symptoms for a pet.
 * It also loads and displays existing symptoms from Firestore in a RecyclerView.
 */
class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var symptomDescriptionInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var symptomList: MutableList<SymptomData>
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var petId: String? = null

    /**
     * Called when the activity is first created. Initializes UI elements,
     * sets up RecyclerView, and listens for user actions.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_add)

        Log.d("SymptomsAddActivity", "onCreate: Activity created")

        petId = intent.getStringExtra("petId")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db = FirebaseFirestore.getInstance()

        symptomInput = findViewById(R.id.symptomInput)
        symptomInput.movementMethod = ScrollingMovementMethod()
        symptomDescriptionInput = findViewById(R.id.symptomDescriptionInput)
        symptomDescriptionInput.movementMethod = ScrollingMovementMethod()
        addSymptomButton = findViewById(R.id.addSymptom)
        recyclerView = findViewById(R.id.petsRecyclerView)

        // Set up RecyclerView
        symptomList = mutableListOf()
        symptomAdapter = SymptomAdapter(symptomList, userId, petId ?: "",
            { deletedSymptom ->
                symptomList.remove(deletedSymptom)
                symptomAdapter.notifyDataSetChanged()
            },
            { selectedSymptom ->
                showEditDialog(selectedSymptom)
            }
        )


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = symptomAdapter

        // Load existing symptoms from Firestore
        petId?.let { loadSymptoms(it) }

        // Back button functionality
        val returnButton = findViewById<ImageButton>(R.id.GoBackButtonSymptoms)
        returnButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        // Add symptom functionality
        addSymptomButton.setOnClickListener {
            if (petId != null) {
                saveSymptomToFirestore(petId!!)
            }
        }
    }

    /**
     * Loads symptoms from Firestore and updates the RecyclerView.
     * This method listens for real-time updates to the symptoms collection.
     *
     * @param petId The pet ID used to fetch the symptoms from Firestore.
     */
    private fun loadSymptoms(petId: String) {
        val dbRef = db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .collection("symptoms")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

        dbRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SymptomsAddActivity", "Error loading symptoms", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val updatedSymptoms = mutableListOf<SymptomData>()

                // Map Firestore documents to SymptomData objects
                for (document in snapshot.documents) {
                    val symptom = document.toObject(SymptomData::class.java)
                    symptom?.let { updatedSymptoms.add(it) }
                }

                // Update the symptom list and notify the adapter
                symptomList.clear()
                symptomList.addAll(updatedSymptoms)
                symptomAdapter.notifyDataSetChanged()

                // Scroll to bottom automatically so the newest symptom is visible
                recyclerView.scrollToPosition(symptomList.size - 1)
            }
        }
    }


    /**
     * Saves a new symptom to Firestore under the current user's pet.
     * The symptom includes the symptom text, description, and timestamp.
     *
     * @param petID The pet ID to associate the symptom with in Firestore.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveSymptomToFirestore(petID: String) {
        val symptomText = symptomInput.text.toString().trim()
        val descriptionText = symptomDescriptionInput.text.toString().trim()


        if (symptomText.isEmpty()) {
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

        // Format timestamp
        val timeString = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)

        val newSymptomRef = db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petID)
            .collection("symptoms")
            .document()

        val symptomData = SymptomData(
            id = newSymptomRef.id,
            symptom = symptomText,
            description = descriptionText, // ✅ Save description
            timestamp = timeString
        )

        // Save symptom to Firestore
        newSymptomRef.set(symptomData)
            .addOnSuccessListener {
                Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                symptomInput.text.clear()
                symptomDescriptionInput.text.clear()

                // Firestore listener will handle updating the RecyclerView
            }
            .addOnFailureListener { e ->
                Log.e("SymptomsAddActivity", "Error adding symptom", e)
                Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Displays a dialog for editing an existing symptom. The user can modify the symptom's
     * title and description.
     *
     * @param symptomData The SymptomData object containing the current symptom information.
     */
    private fun showEditDialog(symptomData: SymptomData) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_symptom, null)
        val editSymptomInput = dialogView.findViewById<EditText>(R.id.editSymptomInput)
        val editDescriptionInput = dialogView.findViewById<EditText>(R.id.editDescriptionInput)

        editSymptomInput.setText(symptomData.symptom)
        editDescriptionInput.setText(symptomData.description)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Edit Symptom")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedSymptom = editSymptomInput.text.toString().trim()
                val updatedDescription = editDescriptionInput.text.toString().trim()

                if (updatedSymptom.isNotEmpty()) {
                    db.collection("users").document(userId).collection("pets")
                        .document(petId!!).collection("symptoms")
                        .document(symptomData.id!!)
                        .update(mapOf("symptom" to updatedSymptom, "description" to updatedDescription))
                }

                val symptomRef = db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId!!)
                    .collection("symptoms")
                    .document(symptomData.id!!)

                val updateData = mutableMapOf<String, Any>("symptom" to updatedSymptom)
                if (updatedDescription.isNotEmpty()) {
                    updateData["description"] = updatedDescription
                } else {
                    updateData["description"] = ""
                }

                symptomRef.update(updateData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }
}