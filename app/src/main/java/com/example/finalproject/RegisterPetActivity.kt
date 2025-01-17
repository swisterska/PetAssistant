package com.example.finalproject

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
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
    private lateinit var registerPetBtn: ImageButton
    private lateinit var selectIconBtn: ImageButton

    private var iconUri: Uri? = null
    private var selectedIconResId: Int? = null
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null

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
                // No additional handling required here, as we're checking in the validateInput method
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // No need to handle this explicitly since we're checking in the validateInput method
            }
        })

        val genderList = mutableListOf("Gender").apply {
            addAll(Gender.values().map { it.name })
        }

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        selectIconBtn.setOnClickListener { openGallery() }

        dobInput.setOnClickListener { showDatePickerDialog() }

        registerPetBtn.setOnClickListener {
            val pet = collectPetData()
            if (pet != null && validateInput(pet)) {
                savePetToFirebase(pet)
            } else {
                Toast.makeText(this, "Invalid input. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun collectPetData(): Pet? {
        val name = petNameInput.text.toString().trim()
        // Collect species value (ensure to handle 'UNKNOWN' case if no selection is made)
        val species = try {
            Species.valueOf(speciesSpinner.selectedItem.toString().uppercase())
        } catch (e: IllegalArgumentException) {
            Species.UNKNOWN // If no valid species is selected, default to 'UNKNOWN'
        }
        val breed = breedInput.text.toString().trim()
        val city = cityInput.text.toString().trim()
        val weight = weightInput.text.toString().toDoubleOrNull() ?: 0.0
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

        // Ensure the species is valid and not the default "*Species"
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
