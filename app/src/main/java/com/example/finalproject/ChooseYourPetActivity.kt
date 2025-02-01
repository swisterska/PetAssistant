package com.example.finalproject


import com.example.finalproject.firebase.Pet
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Species


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Activity that allows users to choose a pet from their list of registered pets.
 * Users can add, edit, and delete pet profiles, as well as navigate to the main page with the selected pet.
 */
class ChooseYourPetActivity : AppCompatActivity() {

    // UI components
    private lateinit var petsRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var petsList: MutableList<Pet>
    private lateinit var addNewPetButton: ImageButton

    /**
     * Called when the activity is first created.
     * Initializes the RecyclerView, sets up the settings menu button, loads pets from Firestore,
     * and handles navigation to other activities when a pet is selected or when the "Add New Pet" button is clicked.
     *
     * @param savedInstanceState The saved instance state from a previous state of this activity, if available.
     */
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

        // Set up settings button
        val settingsButton = findViewById<ImageButton>(R.id.settings)
        settingsButton.setOnClickListener { view ->
            showPopupMenu(view)
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

    /**
     * Loads the list of pets from Firestore for the current user.
     */
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

    /**
     * Shows a dialog for editing pet details.
     */
    fun showEditPetDialog(pet: Pet) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_pet, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Setup the Gender Spinner
        val genderSpinner: Spinner = dialogView.findViewById(R.id.editGenderSpinner)
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

       // Setup the Species Spinner
        val speciesSpinner: Spinner = dialogView.findViewById(R.id.editSpeciesSpinner)
        val speciesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.species_array,
            android.R.layout.simple_spinner_item
        )
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speciesSpinner.adapter = speciesAdapter

        // Convert pet's Gender and Species to match the Spinner values
        val genderString = pet.gender.name.replace("_", " ").lowercase(Locale.ROOT).capitalize(Locale.ROOT)
        val speciesString = pet.species.name.replace("_", " ").lowercase(Locale.ROOT).capitalize(Locale.ROOT)

        // Find the matching index in the spinner
        val genderPosition = genderAdapter.getPosition(genderString).takeIf { it >= 0 } ?: 0
        val speciesPosition = speciesAdapter.getPosition(speciesString).takeIf { it >= 0 } ?: 0

        // Set the spinner selections
        genderSpinner.setSelection(genderPosition)
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

    /**
     * Updates pet details in Firestore.
     */
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

    /**
     * Deletes a pet from Firestore.
     */
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

    /**
     * Displays a date picker dialog for selecting pet's date of birth.
     */
    fun ChooseYourPetActivity.showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)  // Set the selected date in the EditText
        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    /**
     * Displays a popup menu with user settings options, such as editing the profile or logging out.
     *
     * @param view The view that triggers the popup menu.
     */
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_profile -> {
                    showEditProfileDialog()
                    true
                }
                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }


    /**
     * Displays a dialog for editing the user's profile information.
     * The user can update their name, which is retrieved from and saved to Firestore.
     */
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Find views
        val editName = dialogView.findViewById<EditText>(R.id.editProfileName)
        val saveButton = dialogView.findViewById<Button>(R.id.saveProfileChangesButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelProfileChangesButton)

        // Get current user
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val userId = user?.uid ?: return

        // Load existing name from Firestore
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                editName.setText(document.getString("name") ?: "") // Set current name
            }
        }


        // Save changes
        saveButton.setOnClickListener {
            val updatedName = editName.text.toString().trim()

            if (updatedName.isNotEmpty()) {
                updateUserNameInFirestore(userId, updatedName)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /**
     * Updates the user's name in Firestore.
     *
     * @param userId The unique ID of the user in Firestore.
     * @param updatedName The new name to be saved.
     */
    private fun updateUserNameInFirestore(userId: String, updatedName: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.update("name", updatedName)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Deletes the user's account from Firestore.
     * This action is irreversible and removes all user data from Firestore.
     */
    private fun deleteUserFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "User removed.", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove user.", Toast.LENGTH_SHORT).show()
            }
    }


} 