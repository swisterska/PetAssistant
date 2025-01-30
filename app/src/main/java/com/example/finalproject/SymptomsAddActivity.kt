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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SymptomsAddActivity : AppCompatActivity() {

    private lateinit var symptomInput: EditText
    private lateinit var addSymptomButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var symptomList: MutableList<SymptomData>
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var petId: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_add)

        Log.d("SymptomsAddActivity", "onCreate: Activity created")

        petId = intent.getStringExtra("petId")

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db = FirebaseFirestore.getInstance()

        symptomInput = findViewById(R.id.symptomInput)
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
                showEditDialog(selectedSymptom) // Show edit dialog when clicked
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

        addSymptomButton.setOnClickListener {
            if (petId != null) {
                saveSymptomToFirestore(petId!!)
            }
        }
    }

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

                for (document in snapshot.documents) {
                    val symptom = document.toObject(SymptomData::class.java)
                    symptom?.let { updatedSymptoms.add(it) }
                }

                symptomList.clear()
                symptomList.addAll(updatedSymptoms)
                symptomAdapter.notifyDataSetChanged()

                // Scroll to bottom automatically so the newest symptom is visible
                recyclerView.scrollToPosition(symptomList.size - 1)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveSymptomToFirestore(petID: String) {
        val symptomText = symptomInput.text.toString().trim()

        if (TextUtils.isEmpty(symptomText)) {
            Toast.makeText(this, "Please enter a symptom", Toast.LENGTH_SHORT).show()
            return
        }

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
            timestamp = timeString
        )

        newSymptomRef.set(symptomData)
            .addOnSuccessListener {
                Toast.makeText(this, "Symptom added successfully", Toast.LENGTH_SHORT).show()
                symptomInput.text.clear()

                // No need to manually add the symptom since Firestore listener will handle it
            }
            .addOnFailureListener { e ->
                Log.e("SymptomsAddActivity", "Error adding symptom", e)
                Toast.makeText(this, "Failed to add symptom", Toast.LENGTH_SHORT).show()
            }
    }

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

                if (updatedSymptom.isEmpty()) {
                    Toast.makeText(this, "Symptom cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update in Firestore
                val symptomRef = db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId!!)
                    .collection("symptoms")
                    .document(symptomData.id!!)

                symptomRef.update(
                    mapOf(
                        "symptom" to updatedSymptom,
                        "description" to updatedDescription
                    )
                ).addOnSuccessListener {
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