package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

/**
 * Activity for displaying a list of symptoms using a RecyclerView.
 */
class HealthInfoView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SymptomAdapter
    private lateinit var database: DatabaseReference
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

        val userId = intent.getStringExtra("userId") ?: return
        val petId = intent.getStringExtra("petId") ?: return

        database = FirebaseDatabase.getInstance().getReference("users")
            .child(userId).child("pets").child(petId).child("symptoms")

        // Fetch data from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                symptomList.clear()
                for (data in snapshot.children) {
                    val symptom = data.getValue(SymptomData::class.java)
                    if (symptom != null) {
                        symptomList.add(symptom)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
            }
        })

        adapter = SymptomAdapter(symptomList) { deletedItem ->
            symptomList.remove(deletedItem)
            adapter.notifyDataSetChanged()
        }
        recyclerView.adapter = adapter
    }
}
