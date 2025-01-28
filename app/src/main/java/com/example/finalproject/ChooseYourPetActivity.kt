package com.example.finalproject


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.firebase.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChooseYourPetActivity : AppCompatActivity() {

    private lateinit var petsRecyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var petsList: MutableList<Pet>
    private lateinit var addNewPetButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_pet)

        // Initialize the RecyclerView
        petsRecyclerView = findViewById(R.id.petsRecyclerView)
        petsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the pets list and adapter
        petsList = mutableListOf()
        // In ChooseYourPetActivity
        petAdapter = PetAdapter(petsList) { pet ->
            // Pass the selected petId to MainPageActivity
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", pet.id)  // Add petId to the Intent
            startActivity(intent)
        }

        petsRecyclerView.adapter = petAdapter

        // Load pets from Firestore
        loadPets()

        // Set up the "Add New Pet" button
        addNewPetButton = findViewById(R.id.AddNewPetButton)
        addNewPetButton.setOnClickListener {
            val intent = Intent(this, RegisterPetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the Firestore collection of pets for the user
        val db = FirebaseFirestore.getInstance()
        val petsCollectionRef = db.collection("users").document(userId).collection("pets")

        petsCollectionRef.get().addOnSuccessListener { querySnapshot ->
            petsList.clear() // Clear the list before adding new pets
            for (document in querySnapshot) {
                val pet = document.toObject(Pet::class.java)
                petsList.add(pet)
            }
            petAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed
        }.addOnFailureListener { exception ->
            Log.e("ChooseYourPetActivity", "Error loading pets: ${exception.message}")
            Toast.makeText(this, "Failed to load pets", Toast.LENGTH_SHORT).show()
        }
    }
}


