package com.example.finalproject

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Pet
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.*

class RegisterPetActivity : AppCompatActivity() {

    private lateinit var petNameInput: EditText
    private lateinit var speciesInput: EditText
    private lateinit var breedInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var allergiesInput: EditText
    private lateinit var diseasesInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var registerPetBtn: ImageButton
    private lateinit var selectIconBtn: ImageButton

    private var iconUri: Uri? = null
    private var selectedIconResId: Int? = null // Tracks selected predefined icon
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null

    private lateinit var dogButton: ImageButton
    private lateinit var catButton: ImageButton
    private lateinit var rabbitButton: ImageButton
    private lateinit var snakeButton: ImageButton
    private var currentlySelectedButton: ImageButton? = null // Track the selected button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

        // Initialize UI components
        petNameInput = findViewById(R.id.petNameInput)
        speciesInput = findViewById(R.id.speciesInput)
        breedInput = findViewById(R.id.breedInput)
        cityInput = findViewById(R.id.cityInput)
        weightInput = findViewById(R.id.weightInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        allergiesInput = findViewById(R.id.allergiesInput)
        diseasesInput = findViewById(R.id.diseasesInput)
        dobInput = findViewById(R.id.dobInput)
        registerPetBtn = findViewById(R.id.registerPetBtn)
        selectIconBtn = findViewById(R.id.selectIconBtn)

        dogButton = findViewById(R.id.DogButton)
        catButton = findViewById(R.id.caticon)
        rabbitButton = findViewById(R.id.rabbiticon)
        snakeButton = findViewById(R.id.snakeicon)

        // Set up Gender Spinner
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Gender.values())
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Icon selection logic
        dogButton.setOnClickListener { selectPredefinedIcon(dogButton, R.drawable.dogicon) }
        catButton.setOnClickListener { selectPredefinedIcon(catButton, R.drawable.caticon) }
        rabbitButton.setOnClickListener { selectPredefinedIcon(rabbitButton, R.drawable.rabbiticon) }
        snakeButton.setOnClickListener { selectPredefinedIcon(snakeButton, R.drawable.snakeicon) }
        selectIconBtn.setOnClickListener { openGallery() }

        // Date Picker
        dobInput.setOnClickListener { showDatePickerDialog() }

        // Register button
        registerPetBtn.setOnClickListener {
            val pet = collectPetData()
            if (validateInput(pet)) savePetToFirebase(pet)
        }
    }

    private fun selectPredefinedIcon(button: ImageButton, iconResId: Int) {
        // Reset all buttons to no border (transparent background)
        resetButtonHighlight(dogButton)
        resetButtonHighlight(catButton)
        resetButtonHighlight(rabbitButton)
        resetButtonHighlight(snakeButton)

        // Apply border (frame) for the selected button
        button.setBackgroundResource(R.drawable.selectediconframe)

        // Set the icon for the selected button
        selectedIconResId = iconResId
        iconUri = null
    }

    private fun resetButtonHighlight(button: ImageButton) {
        // Remove the border/frame for the unselected buttons
        button.setBackgroundColor(Color.TRANSPARENT)
    }




    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun collectPetData(): Pet {
        val name = petNameInput.text.toString()
        val species = speciesInput.text.toString()
        val breed = breedInput.text.toString()
        val city = cityInput.text.toString()
        val weight = weightInput.text.toString().toDoubleOrNull() ?: 0.0
        val gender = Gender.valueOf(genderSpinner.selectedItem.toString())
        val allergies = allergiesInput.text.toString().split(",").map { it.trim() }.toMutableList()
        val diseases = diseasesInput.text.toString().split(",").map { it.trim() }.toMutableList()
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
        if (pet.species.isBlank()) {
            Toast.makeText(this, "Species is required", Toast.LENGTH_SHORT).show()
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
                // Navigate back to ChooseYourPetActivity
                val intent = Intent(this, ChooseYourPetActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish() // Close the current activity
            } else {
                Toast.makeText(this, "Failed to register pet", Toast.LENGTH_SHORT).show()
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            iconUri = data?.data
            selectedIconResId = null
        }
    }
}
