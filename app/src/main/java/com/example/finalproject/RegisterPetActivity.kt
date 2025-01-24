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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.*
import com.google.firebase.auth.FirebaseAuth


/**
 * RegisterPetActivity handles the process of registering a new pet by allowing the user to input pet details,
 * select a species, gender, date of birth, and upload an icon. The pet data is then stored in Firebase.
 */
class RegisterPetActivity : AppCompatActivity() {

    private lateinit var petNameInput: EditText
    private lateinit var speciesSpinner: Spinner
    private lateinit var breedInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var allergiesInput: EditText
    private lateinit var diseasesInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var registerPetBtn: Button
    private lateinit var selectIconBtn: ImageButton
    private var selectedButton: ImageButton? = null


    private var iconUri: Uri? = null
    private var selectedIconResId: Int? = null
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null

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
        cityInput = findViewById(R.id.cityInput)
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
        })

        val genderList = mutableListOf("Gender").apply {
            addAll(Gender.values().map { it.name })
        }

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        val dogButton = findViewById<ImageButton>(R.id.DogButton)
        val catButton = findViewById<ImageButton>(R.id.caticon)
        val rabbitButton = findViewById<ImageButton>(R.id.rabbiticon)
        val snakeButton = findViewById<ImageButton>(R.id.snakeicon)
        val selectIconBtn = findViewById<ImageButton>(R.id.selectIconBtn)

        val iconButtons = listOf(dogButton, catButton, rabbitButton, snakeButton, selectIconBtn)

        for (button in iconButtons) {
            button.setOnClickListener {
                selectedButton?.isSelected = false  // Deselect previous button
                button.isSelected = true  // Select current button
                selectedButton = button  // Update selected button
            }
        }

        selectIconBtn.setOnClickListener { openGallery() }

        dobInput.setOnClickListener { showDatePickerDialog() }

        registerPetBtn.setOnClickListener {
            Log.d("RegisterPetActivity", "Register button clicked")
            val pet = collectPetData()
            if (pet != null && validateInput(pet)) {
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
    private fun collectPetData(): Pet? {
        val name = petNameInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val species = try {
            Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Species.UNKNOWN // Default if no selection is made
        }
        val breed = breedInput.text.toString().trim().takeIf { it.isNotEmpty() } // Nullable breed
        val city = cityInput.text.toString().trim().takeIf { it.isNotEmpty() } // Nullable city
        val weight = weightInput.text.toString().toDoubleOrNull() // Nullable weight
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

        val iconUriString = iconUri?.toString()

        return Pet(
            iconUri = iconUriString,
            name = name ?: "",  // Name is required
            species = species,
            breed = breed ?: "",  // If breed is null, default to an empty string
            city = city ?: "",
            weight = weight ?: 0.0, // If weight is null, default to 0.0
            gender = gender,
            allergies = allergies,
            diseases = diseases,
            dob = selectedDob
        )
    }

    /**
     * Validates the input fields to ensure the pet's details are correct.
     *
     * @param pet The pet object containing the details to be validated.
     * @return true if the input is valid, false otherwise.
     */
    private fun validateInput(pet: Pet): Boolean {
        if (pet.name.isBlank()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true // Only name is mandatory; other fields can be null or empty
    }


    /**
     * Saves the pet data to Firebase. If an icon was selected, it will be uploaded to Firebase Storage.
     *
     * @param pet The pet object to be saved to Firebase.
     */
    private fun savePetToFirebase(pet: Pet) {
        if (iconUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("pet_icons/${pet.name}_${System.currentTimeMillis()}.jpg")
            storageRef.putFile(iconUri!!).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    pet.iconUri = uri.toString()
                    savePetDetails(pet)
                }
            }
        } else {
            savePetDetails(pet)
        }
    }

    /**
     * Saves the pet details to Firebase Realtime Database.
     *
     * @param pet The pet object to be saved.
     */
    private fun savePetDetails(pet: Pet) {
        // Get the currently logged-in user's ID (Firebase Authentication is used)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the specific user's "pets" node
        val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("pets")

        // Generate a unique key for the pet inside the user's "pets" array
        val petKey = dbRef.push().key

        if (petKey != null) {
            dbRef.child(petKey).setValue(pet).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterPetActivity", "Pet registered successfully under user: $userId")
                    Toast.makeText(this, "New pet registered successfully!", Toast.LENGTH_SHORT).show()

                    // Redirect user after successful registration
                    val intent = Intent(this, ChooseYourPetActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("RegisterPetActivity", "Failed to register pet: ${task.exception?.message}")
                    Toast.makeText(this, "Failed to register pet.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("RegisterPetActivity", "Failed to generate pet key.")
            Toast.makeText(this, "Error generating pet key.", Toast.LENGTH_SHORT).show()
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
        datePickerDialog.show()
    }

    /**
     * Handles the result from the gallery activity when an icon is selected.
     *
     * @param requestCode The request code for the activity result.
     * @param resultCode The result code returned by the activity.
     * @param data The intent data returned by the activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            iconUri = data?.data
            selectedIconResId = null
        }
    }
}
