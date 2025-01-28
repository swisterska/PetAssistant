package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for displaying a list of symptoms using a RecyclerView.
 */
class HealthInfoView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SymptomAdapter
    private lateinit var db: FirebaseFirestore
    private var symptomList = mutableListOf<SymptomData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_info_view)

        val returnButton = findViewById<ImageButton>(R.id.goBackButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve userId and petId from intent
        val userId = intent.getStringExtra("userId") ?: return
        val petId = intent.getStringExtra("petId") ?: return

        // Initialize Firestore reference
        db = FirebaseFirestore.getInstance()

        // Fetch data from Firestore
        db.collection("users")
            .document(userId)
            .collection("pets")
            .document(petId)
            .collection("symptoms")
            .get()
            .addOnSuccessListener { result ->
                symptomList.clear()
                for (document in result) {
                    val symptom = document.toObject(SymptomData::class.java).copy(id = document.id)
                    symptomList.add(symptom)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace() // Log error
            }

        // Initialize the adapter with userId and petId
        adapter = SymptomAdapter(symptomList, userId, petId) { deletedItem ->
            symptomList.remove(deletedItem)
            adapter.notifyDataSetChanged()
        }
        recyclerView.adapter = adapter
    }
}