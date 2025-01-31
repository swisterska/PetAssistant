package com.example.finalproject

// Import your custom classes (Pet, Gender, Species) from the firebase package
import com.example.finalproject.firebase.Pet
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Species

// Other necessary imports
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter


class ChooseYourPetActivity : AppCompatActivity() {

    private lateinit var petsRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var petsList: MutableList<Pet>
    private lateinit var addNewPetButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChooseYourPetActivity", "onCreate started")

        setContentView(R.layout.activity_choose_your_pet)

        // Initialize the RecyclerView
        petsRecyclerView = findViewById(R.id.petsRecyclerView)
        petsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the pets list and adapter
        petsList = mutableListOf()
        petAdapter = PetAdapter(petsList) { pet ->
            Log.d("ChooseYourPetActivity", "Pet clicked: ${pet.id}")

            // Pass the selected petId to MainPageActivity
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }

        petsRecyclerView.adapter = petAdapter
        Log.d("ChooseYourPetActivity", "RecyclerView and adapter initialized")

        // Load pets from Firestore
        loadPets()

        // Set up the "Add New Pet" button
        addNewPetButton = findViewById(R.id.addNewPetButton)
        addNewPetButton.setOnClickListener {
            Log.d("ChooseYourPetActivity", "Add New Pet button clicked")
            val intent = Intent(this, RegisterPetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("ChooseYourPetActivity", "Loading pets for user: $userId")

        val db = FirebaseFirestore.getInstance()
        val petsCollectionRef = db.collection("users").document(userId).collection("pets")

        petsCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                petsList.clear()
                for (document in querySnapshot) {
                    val pet = document.toObject(Pet::class.java)
                    pet.id = document.id
                    petsList.add(pet)
                }
                petAdapter.notifyDataSetChanged()  // Ensure RecyclerView is notified of changes
            }

            .addOnFailureListener { exception ->
                Log.e("ChooseYourPetActivity", "Error loading pets: ${exception.message}")
                Toast.makeText(this, "Failed to load pets", Toast.LENGTH_SHORT).show()
            }
    }

    fun showEditPetDialog(pet: Pet) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_pet, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Setup the Gender Spinner
        val genderSpinner: Spinner = dialogView.findViewById(R.id.editGenderSpinner)
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array, // Create this string array resource
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Setup the Species Spinner
        val speciesSpinner: Spinner = dialogView.findViewById(R.id.editSpeciesSpinner)
        val speciesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.species_array, // Create this string array resource
            android.R.layout.simple_spinner_item
        )
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speciesSpinner.adapter = speciesAdapter

        // Get the Gender and Species from the Pet object
        val genderPosition = genderAdapter.getPosition(pet.gender.name.capitalize())
        genderSpinner.setSelection(genderPosition)

        val speciesPosition = speciesAdapter.getPosition(pet.species.name.capitalize())
        speciesSpinner.setSelection(speciesPosition)

        // Pre-fill pet data
        val editPetName = dialogView.findViewById<EditText>(R.id.editPetName)
        val editBreed = dialogView.findViewById<EditText>(R.id.editBreed)
        val editWeight = dialogView.findViewById<EditText>(R.id.editWeight)
        val editAllergies = dialogView.findViewById<EditText>(R.id.editAllergies)
        val editDiseases = dialogView.findViewById<EditText>(R.id.editDiseases)
        val editDob = dialogView.findViewById<EditText>(R.id.editDob)
        val saveChangesButton = dialogView.findViewById<Button>(R.id.saveChangesButton)
        val deletePetButton = dialogView.findViewById<ImageButton>(R.id.deletePetButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Pre-fill other fields
        editPetName.setText(pet.name)
        editBreed.setText(pet.breed)
        editWeight.setText(pet.weight.toString())
        editAllergies.setText(pet.allergies.joinToString(", "))
        editDiseases.setText(pet.diseases.joinToString(", "))
        editDob.setText(pet.dob ?: "")

        // Setup date picker for DOB
        editDob.setOnClickListener { showDatePickerDialog(editDob) }

        saveChangesButton.setOnClickListener {
            val updatedPet = pet.copy(
                name = editPetName.text.toString().trim(),
                breed = editBreed.text.toString().trim(),
                weight = editWeight.text.toString().toDoubleOrNull() ?: pet.weight,
                allergies = editAllergies.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                diseases = editDiseases.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                dob = editDob.text.toString().trim(),
                gender = Gender.valueOf(genderSpinner.selectedItem.toString().uppercase()),  // Corrected reference
                species = Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())  // Corrected reference
            )

            updatePetInFirestore(updatedPet)
            dialog.dismiss()
        }


        deletePetButton.setOnClickListener {
            deletePetFromFirestore(pet.id)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun updatePetInFirestore(pet: Pet) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("pets").document(pet.id)
            .set(pet)
            .addOnSuccessListener {
                Toast.makeText(this, "Pet details updated!", Toast.LENGTH_SHORT).show()
                loadPets() // Refresh list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update pet.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePetFromFirestore(petId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("pets").document(petId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Pet removed.", Toast.LENGTH_SHORT).show()
                loadPets() // Refresh list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove pet.", Toast.LENGTH_SHORT).show()
            }
    }

    fun ChooseYourPetActivity.showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)  // Set the selected date in the EditText
        }, year, month, day)

        datePickerDialog.show()
    }
}