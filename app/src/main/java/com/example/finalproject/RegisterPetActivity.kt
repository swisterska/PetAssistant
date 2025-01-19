package com.example.finalproject

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
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

class RegisterPetActivity : AppCompatActivity() {

    private lateinit var petNameInput: EditText
    private lateinit var speciesSpinner: Spinner
    private lateinit var breedInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var weightValue: TextView
    private lateinit var genderSpinner: Spinner
    private lateinit var allergiesInput: EditText
    private lateinit var diseasesInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var registerPetBtn: ImageButton
    private lateinit var selectIconBtn: ImageButton
    private lateinit var dogButton: ImageButton
    private lateinit var catButton: ImageButton
    private lateinit var rabbitButton: ImageButton
    private lateinit var snakeButton: ImageButton

    private var iconUri: Uri? = null
    private var selectedIconResId: Int? = null
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null
    private var selectedWeight: Int = 0 // Stores the selected weight
    private var selectedIconButton: ImageButton? = null // Track the selected icon button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

        // Initialize views
        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, ChooseYourPetActivity::class.java)
            startActivity(intent)
        }

        petNameInput = findViewById(R.id.petNameInput)
        speciesSpinner = findViewById(R.id.speciesInput)
        breedInput = findViewById(R.id.breedInput)
        cityInput = findViewById(R.id.cityInput)
        weightValue = findViewById(R.id.weightValue)
        genderSpinner = findViewById(R.id.genderSpinner)
        allergiesInput = findViewById(R.id.allergiesInput)
        allergiesInput.movementMethod = ScrollingMovementMethod()
        diseasesInput = findViewById(R.id.diseasesInput)
        diseasesInput.movementMethod = ScrollingMovementMethod()
        dobInput = findViewById(R.id.dobInput)
        registerPetBtn = findViewById(R.id.registerPetBtn)
        selectIconBtn = findViewById(R.id.selectIconBtn)
        dogButton = findViewById(R.id.DogButton)
        catButton = findViewById(R.id.caticon)
        rabbitButton = findViewById(R.id.rabbiticon)
        snakeButton = findViewById(R.id.snakeicon)

        // Set up species spinner
        val speciesList = mutableListOf("*Species").apply {
            addAll(Species.values().map { it.name })
        }
        val speciesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, speciesList)
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speciesSpinner.adapter = speciesAdapter

        // Set up gender spinner
        val genderList = mutableListOf("Gender").apply {
            addAll(Gender.values().map { it.name })
        }
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Open gallery for icon selection
        selectIconBtn.setOnClickListener { openGallery() }

        // Open date picker dialog for DOB
        dobInput.setOnClickListener { showDatePickerDialog() }

        // Open weight picker dialog on weightText click
        weightValue.setOnClickListener { showWeightPickerDialog() }

        // Register pet on button click
        registerPetBtn.setOnClickListener {
            val pet = collectPetData()
            if (pet != null && validateInput(pet)) {
                savePetToFirebase(pet)
            } else {
                Toast.makeText(this, "Invalid input. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set listeners for pet icons
        dogButton.setOnClickListener { setSelectedIcon(dogButton) }
        catButton.setOnClickListener { setSelectedIcon(catButton) }
        rabbitButton.setOnClickListener { setSelectedIcon(rabbitButton) }
        snakeButton.setOnClickListener { setSelectedIcon(snakeButton) }
    }

    private fun setSelectedIcon(selectedButton: ImageButton) {
        // Reset background for previously selected button (if any)
        selectedIconButton?.setBackgroundResource(android.R.color.transparent)

        // Apply the border to the currently selected button
        selectedButton.setBackgroundResource(R.drawable.selectediconframe)

        // Update the reference to the currently selected button
        selectedIconButton = selectedButton
        selectedIconResId = when (selectedButton.id) {
            R.id.DogButton -> R.drawable.dogicon
            R.id.caticon -> R.drawable.caticon
            R.id.rabbiticon -> R.drawable.rabbiticon
            R.id.snakeicon -> R.drawable.snakeicon
            else -> null
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

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

    private fun showWeightPickerDialog() {
        val weightPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = 100
            value = selectedWeight
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this)
            .setTitle("Select Weight (kg)")
            .setView(weightPicker)
            .setPositiveButton("OK") { _, _ ->
                selectedWeight = weightPicker.value
                weightValue.text = selectedWeight.toString() // Update weight text
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun collectPetData(): Pet? {
        val name = petNameInput.text.toString().trim()
        val species = try {
            Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Species.UNKNOWN
        }
        val breed = breedInput.text.toString().trim()
        val city = cityInput.text.toString().trim()
        val weight = selectedWeight.toDouble()
        val gender = try {
            Gender.valueOf(genderSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Gender.UNKNOWN
        }
        val allergies = allergiesInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        val diseases = diseasesInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        val iconUriString = iconUri?.toString()
            ?: "android.resource://$packageName/${selectedIconResId ?: R.drawable.defaulticon}"

        return Pet(
            iconUri = iconUriString,
            name = name,
            species = species,
            breed = breed,
            city = city,
            weight = weight,
            gender = gender,
            allergies = allergies,
            diseases = diseases,
            dob = selectedDob
        )
    }

    private fun validateInput(pet: Pet): Boolean {
        if (pet.name.isBlank()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pet.species.name == "*Species") {
            Toast.makeText(this, "Please choose a species from the list", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

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

    private fun savePetDetails(pet: Pet) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("pets")
        dbRef.push().setValue(pet).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "New pet registered successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ChooseYourPetActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to register pet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            iconUri = data?.data
            // Remove the selection border from the previous icon button (if any)
            selectedIconButton?.setBackgroundResource(android.R.color.transparent)

            // Set the selected image as the background of the selectIconBtn
            selectIconBtn.setImageURI(iconUri)
            selectedIconResId = null // Clear any previously selected icon
            // Apply the border around the selected icon (selectIconBtn)
            selectIconBtn.setBackgroundResource(R.drawable.selectediconframe)
        }
    }
}
