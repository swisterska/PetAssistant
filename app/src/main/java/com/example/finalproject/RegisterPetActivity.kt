package com.example.finalproject

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
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Pet
import com.example.finalproject.firebase.Species
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter


/**
 * RegisterPetActivity handles the process of registering a new pet by allowing the user to input pet details,
 * select a species, gender, date of birth, and upload an icon. The pet data is then stored in Firebase.
 */
class RegisterPetActivity : AppCompatActivity() {

    private lateinit var dogIcon: ImageButton
    private lateinit var catIcon: ImageButton
    private lateinit var rabbitIcon: ImageButton
    private lateinit var snakeIcon: ImageButton
    private lateinit var petNameInput: EditText
    private lateinit var speciesSpinner: Spinner
    private lateinit var breedInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var allergiesInput: EditText
    private lateinit var diseasesInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var registerPetBtn: Button
    private lateinit var selectIconBtn: ImageButton
    private var selectedButton: ImageButton? = null

    private var selectedIconUri: String? = null  // To store the selected icon URI
    private var iconUri: Uri? = null
    private var selectedIconResId: Int? = null
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null

    private val whiteBorder = R.drawable.selectediconframe

    /**
     * Called when the activity is created. Sets up the UI components, including spinners for species and gender,
     * and click listeners for the buttons.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, ChooseYourPetActivity::class.java)
            startActivity(intent)
        }

        petNameInput = findViewById(R.id.petNameInput)
        speciesSpinner = findViewById(R.id.speciesInput)
        breedInput = findViewById(R.id.breedInput)
        weightInput = findViewById(R.id.weightInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        allergiesInput = findViewById(R.id.allergiesInput)
        allergiesInput.movementMethod = ScrollingMovementMethod()
        diseasesInput = findViewById(R.id.diseasesInput)
        diseasesInput.movementMethod = ScrollingMovementMethod()
        dobInput = findViewById(R.id.dobInput)
        registerPetBtn = findViewById(R.id.registerPetBtn)
        selectIconBtn = findViewById(R.id.selectIconBtn)


        val speciesList = mutableListOf("*Species").apply {
            addAll(Species.values().map { it.name })
        }

        val speciesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, speciesList)
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speciesSpinner.adapter = speciesAdapter

        speciesSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {

            }
        }
        )

        val genderList = mutableListOf("Gender").apply {
            addAll(Gender.values().map { it.name })
        }

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        dogIcon = findViewById(R.id.DogButton)
        catIcon = findViewById(R.id.caticon)
        rabbitIcon = findViewById(R.id.rabbiticon)
        snakeIcon = findViewById(R.id.snakeicon)
        selectIconBtn = findViewById(R.id.selectIconBtn)

        // List of icon buttons to iterate
        val iconButtons = listOf(dogIcon, catIcon, rabbitIcon, snakeIcon, selectIconBtn)

        // Set the initial click listeners for the icons
        for (button in iconButtons) {
            button.setOnClickListener {
                // Reset border for previously selected button
                selectedButton?.background = null

                // Set white border around the clicked icon
                button.background = getDrawable(whiteBorder)

                // Save the clicked button as the selected button
                selectedButton = button

                // Handle the selection of each icon
                when (button) {
                    dogIcon -> savePetProfileIcon("dogicon")
                    catIcon -> savePetProfileIcon("caticon")
                    rabbitIcon -> savePetProfileIcon("rabbiticon")
                    snakeIcon -> savePetProfileIcon("snakeicon")
                    selectIconBtn -> openGallery()
                }
            }
        }

        selectIconBtn.setOnClickListener { openGallery() }

        dobInput.setOnClickListener { showDatePickerDialog() }

        registerPetBtn.setOnClickListener {
            Log.d("RegisterPetActivity", "Register button clicked")
            val pet = collectPetData()
            if (pet != null && validateInput(pet)) {
                Log.d("RegisterPetActivity", "Pet data valid, saving to Firebase")
                savePetToFirebase(pet)
            } else {
                Toast.makeText(this, "Invalid input. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     * Opens the gallery to allow the user to select a pet icon image.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    /**
     * Collects all the pet details entered by the user in the form and returns a Pet object.
     *
     * @return Pet object or null if data collection fails.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectPetData(): Pet? {
        val name = petNameInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val species = try {
            Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Species.UNKNOWN // Default if no selection is made
        }
        val breed = breedInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val weight = weightInput.text.toString().toDoubleOrNull()
        val gender = try {
            Gender.valueOf(genderSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Gender.UNKNOWN
        }
        val allergies = allergiesInput.text.toString()
            .split(",").map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        val diseases = diseasesInput.text.toString()
            .split(",").map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        val dobString = selectedDob?.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val iconUri = selectedIconResId?.toString() ?: "" // Store the resource ID as a string

        // Add logging to verify the collected data
        Log.d("RegisterPetActivity", "Collected Pet Data: Name=$name, Species=$species, Breed=$breed, Weight=$weight, Gender=$gender")

        return Pet(
            iconUri,
            "",
            name ?: "",
            dobString,
            species,
            breed ?: "",
            allergies,
            diseases,
            weight ?: 0.0,
            gender
        )
    }


    /**
     * Validates the input fields to ensure the pet's details are correct.
     *
     * @param pet The pet object containing the details to be validated.
     * @return true if the input is valid, false otherwise.
     */
    private fun validateInput(pet: Pet): Boolean {
        var errorMessage: String? = null  // Store the first error message found

        if (pet.name.isBlank()) {
            errorMessage = "Name is required"
        } else if (pet.species == Species.UNKNOWN) {
            errorMessage = "Please select a valid species"
        }

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            return false  // Stop validation immediately after the first error
        }

        return true  // If no errors, return true
    }



    /**
     * Saves the pet data to Firebase. If an icon was selected, it will be uploaded to Firebase Storage.
     * After the icon is uploaded, the pet data will be saved to Firestore.
     *
     * @param pet The pet object to be saved to Firebase.
     */
    private fun savePetToFirebase(pet: Pet) {
        // Check if a valid icon has been selected
        val iconName = when (selectedIconResId) {
            R.drawable.dogicon -> "dogicon"
            R.drawable.caticon -> "caticon"
            R.drawable.rabbiticon -> "rabbiticon"
            R.drawable.snakeicon -> "snakeicon"
            else -> "default_icon"  // Use a default icon if no valid icon is selected
        }

        // Save the icon name to the pet object
        pet.iconUri = iconName

        // Save the pet details to Firestore
        savePetDetails(pet)
    }


    /**
     * Saves the pet details to Firestore.
     *
     * @param pet The pet object to be saved.
     */
    private fun savePetDetails(pet: Pet) {
        // Get the currently logged-in user's ID (Firebase Authentication is used)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the Firestore collection where the pets will be stored
        val dbRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("pets")

        // Generate a document reference for the pet (Firestore automatically generates a unique ID for each pet)
        val petRef = dbRef.document()

        // Save the pet data to Firestore
        petRef.set(pet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("RegisterPetActivity", "Pet registered successfully under user: $userId")
                Toast.makeText(this, "New pet registered successfully!", Toast.LENGTH_SHORT).show()

                // Redirect user after successful registration
                goToMainPageActivity(petId = petRef.id)
            } else {
                Log.e("RegisterPetActivity", "Failed to register pet: ${task.exception?.message}")
                Toast.makeText(this, "Failed to register pet.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    /**
     * Displays a DatePicker dialog for the user to select the pet's date of birth.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDob = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                dobInput.setText(selectedDob.toString())
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    /**
     * Handles the result from the gallery activity when an image is selected.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                iconUri = it
                selectedIconUri = it.toString()  // Save URI of selected image
                // Update the ImageButton with the selected icon (optional)
                selectIconBtn.setImageURI(iconUri)
            }
        }
    }


    /**
     * Navigates to the MainPageActivity after successful pet registration.
     * Passes the user's email and the registered pet's ID as extras.
     */
    open fun goToMainPageActivity(petId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.orEmpty()

        val intent = Intent(this, MainPageActivity::class.java).apply {
            putExtra("uID", email) // Pass user email
            putExtra("petId", petId) // Pass petId
        }
        startActivity(intent)
        finish() // Close current activity to prevent going back
    }

    /**
     * Saves the selected pet profile icon to Firestore.
     */
    // Method to save the selected icon to Firestore
    private fun savePetProfileIcon(iconName: String) {
        selectedIconResId = resources.getIdentifier(iconName, "drawable", packageName)

        // Save the icon name as a string (e.g., "dogicon") in Firestore
        selectedIconUri = iconName  // Save the resource name (not the ID)
    }


}