package com.example.finalproject


import com.example.finalproject.firebase.Pet
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Species


import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.EmailAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser


/**
 * Activity that allows users to choose a pet from their list of registered pets.
 * Users can add, edit, and delete pet profiles, as well as navigate to the main page with the selected pet.
 */
class ChooseYourPetActivity : AppCompatActivity() {

    // UI components, variables will be initialized later (in the onCreate() method)
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

        // Settings button
        val settingsButton = findViewById<ImageButton>(R.id.settings)
        settingsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        petsRecyclerView.adapter = petAdapter
        Log.d("ChooseYourPetActivity", "RecyclerView and adapter initialized")

        // Loads pets from Firestore
        loadPets()

        // "Add New Pet" button
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return //retrieves the currently authenticated user's unique ID
        Log.d("ChooseYourPetActivity", "Loading pets for user: $userId")

        val db = FirebaseFirestore.getInstance()
        val petsCollectionRef = db.collection("users").document(userId).collection("pets")

        petsCollectionRef.get() //fetches the data from the pets subcollection of the user
            .addOnSuccessListener { querySnapshot ->
                petsList.clear() //clear so we don't double
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

        // Convert pet's Gender and Species
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
            val updatedName = editPetName.text.toString().trim()

            if (updatedName.isEmpty()) {
                Toast.makeText(this, "Pet name cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedPet = pet.copy(
                name = updatedName,
                breed = editBreed.text.toString().trim(),
                weight = editWeight.text.toString().toDoubleOrNull() ?: pet.weight,
                allergies = editAllergies.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                diseases = editDiseases.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                dob = editDob.text.toString().trim(),
                gender = Gender.valueOf(genderSpinner.selectedItem.toString().uppercase()),
                species = Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())
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
                loadPets() // refresh list
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
        val editUsername = dialogView.findViewById<EditText>(R.id.editProfileName) // Using editProfileName but storing username
        val editEmail = dialogView.findViewById<EditText>(R.id.editProfileEmail)
        val saveButton = dialogView.findViewById<Button>(R.id.saveProfileChangesButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelProfileChangesButton)
        val deleteAccountButton = dialogView.findViewById<ImageButton>(R.id.deleteAccountButton)

        // Get current user
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val userId = user?.uid ?: return

        // Pre-fill email with current user email
        editEmail.setText(user?.email ?: "")

        // Load existing username from Firestore (replacing name with username)
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentUsername = document.getString("username") ?: "" // Fetching username correctly
                editUsername.setText(currentUsername) // Set username in EditText
            } else {
                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile info", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val updatedUsername = editUsername.text.toString().trim()
            val updatedEmail = editEmail.text.toString().trim()

            if (updatedUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update username in Firestore
            updateUsername(userId, updatedUsername)

            // Update email if changed
            if (updatedEmail.isNotEmpty() && updatedEmail != user?.email) {
                updateEmail(user, updatedEmail)
            }

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        deleteAccountButton.setOnClickListener {
            confirmAccountDeletion()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(800, 1500)

    }



    /**
     * Updates the user's email address after re-authenticating with their current password.
     * If successful, sends a verification email to the new email address.
     * Once the user verifies the new email, the email address is updated and the user is logged out.
     *
     * @param user The current authenticated user whose email will be updated.
     * @param newEmail The new email address to be set for the user.
     */

    private fun updateEmail(user: FirebaseUser, newEmail: String) {
        val passwordInput = EditText(this)
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Re-authentication Required")
            .setMessage("Enter your current password to update your email:")
            .setView(passwordInput)
            .setPositiveButton("Confirm") { _, _ ->
                val password = passwordInput.text.toString().trim()

                if (password.isEmpty()) {
                    Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(user.email!!, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener {

                        user.verifyBeforeUpdateEmail(newEmail)
                            .addOnSuccessListener {

                                Toast.makeText(this, "A verification email has been sent to $newEmail. Please verify to complete the update.", Toast.LENGTH_LONG).show()


                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(this, LogRegActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to send verification email: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Re-authentication failed. Check your password!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




    /**
     * Displays a dialog to prompt the user for their password before updating their email.
     *
     * @param newEmail The new email address the user wants to update to.
     * @param parentDialog The dialog from which this function is triggered (to be dismissed after update).
     */

    private fun showReauthenticationDialog(newEmail: String, parentDialog: AlertDialog) {
        val reauthView = layoutInflater.inflate(R.layout.dialog_reauthenticate, null)
        val reauthDialog = AlertDialog.Builder(this)
            .setView(reauthView)
            .create()

        val passwordInput = reauthView.findViewById<EditText>(R.id.passwordInput)
        val confirmButton = reauthView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = reauthView.findViewById<Button>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            val password = passwordInput.text.toString().trim()
            if (password.isEmpty()) {
                Toast.makeText(this, "Enter your password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            reauthenticateAndUpdateEmail(newEmail, password, parentDialog, reauthDialog)
        }

        cancelButton.setOnClickListener { reauthDialog.dismiss() }

        reauthDialog.show()
    }

    /**
     * Re-authenticates the user using their password and updates their email address.
     *
     * @param newEmail The new email to be set for the user.
     * @param password The user's current password for authentication.
     * @param parentDialog The parent dialog (profile edit dialog) to be dismissed upon successful update.
     * @param reauthDialog The reauthentication dialog to be dismissed after completing authentication.
     */

    private fun reauthenticateAndUpdateEmail(newEmail: String, password: String, parentDialog: AlertDialog, reauthDialog: AlertDialog) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                        parentDialog.dismiss()
                        reauthDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Reauthentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Updates the user's name in Firestore.
     *
     * @param userId The ID of the user.
     * @param newName The updated name of the user.
     */

    private fun updateUsername(userId: String, newUsername: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("username", newUsername) // Using "username" instead of "name"
            .addOnSuccessListener {
                Log.d("Firestore", "Username updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating username", e)
            }
    }


    /**
     * Displays a confirmation dialog before account deletion.
     */

    private fun confirmAccountDeletion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Apply the custom background
        dialog.window?.setBackgroundDrawableResource(R.drawable.sad)

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val password = passwordInput.text.toString().trim()

            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        deleteUserAccount() // Proceed with deletion
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(800, 1500)

    }



    /**
     * Deletes the user's Firebase account.
     */

    /**
     * Deletes the user from Firestore and Firebase Authentication.
     */
    private fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        // First, retrieve all subcollections dynamically
        userRef.collection("pets").get()
            .addOnSuccessListener { petsSnapshot ->
                val batch = db.batch()

                // Delete each pet document
                for (petDoc in petsSnapshot.documents) {
                    batch.delete(petDoc.reference)
                }

                // Once all pets are removed, delete user document
                batch.delete(userRef)

                // Commit batch deletion
                batch.commit()
                    .addOnSuccessListener {
                        // Now delete the user from Firebase Authentication
                        deleteFirebaseUser(user)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Delete the user from Firebase Authentication after their data is removed from Firestore.
     */
    /**
     * Deletes the user from Firebase Authentication.
     */
    private fun deleteFirebaseUser(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



}