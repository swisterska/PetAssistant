import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import com.example.finalproject.firebase.Gender
import com.example.finalproject.firebase.Pet
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class RegisterPetActivity : AppCompatActivity() {

    private lateinit var petIcon: ImageView
    private lateinit var petNameInput: EditText
    private lateinit var speciesInput: EditText
    private lateinit var breedInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var allergiesInput: EditText
    private lateinit var diseasesInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var registerPetBtn: Button
    private lateinit var selectIconBtn: Button

    private var iconUri: Uri? = null
    private val GALLERY_REQUEST_CODE = 100
    private var selectedDob: LocalDate? = null // Store selected Date of Birth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

        // Initialize UI components
        petIcon = findViewById(R.id.petIcon)
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

        // Set up Gender Spinner
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Gender.values())
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Select icon from gallery
        selectIconBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        // Date Picker for Date of Birth
        dobInput.setOnClickListener {
            showDatePickerDialog()
        }

        // Register pet
        registerPetBtn.setOnClickListener {
            val pet = collectPetData()
            if (validateInput(pet)) {
                savePetToFirebase(pet)
            }
        }
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

        return Pet(
            iconUri = iconUri?.toString(),
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
        // Upload profile picture to Firebase Storage (if selected)
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
        val databaseRef = FirebaseDatabase.getInstance().getReference("pets")
        val petId = databaseRef.push().key ?: ""
        pet.id = petId
        databaseRef.child(petId).setValue(pet).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Pet registered successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to register pet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Date Picker dialog
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                dobInput.setText(formattedDate)
                selectedDob = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // Handle result of image selection from gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            iconUri = data?.data
            petIcon.setImageURI(iconUri) // Show the selected picture
        }
    }
}
