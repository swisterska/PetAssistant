package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HealthInfoView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthInfoAdapter
    private lateinit var database: DatabaseReference
    private var itemList = mutableListOf<HealthInfoData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_info_view)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        database = FirebaseDatabase.getInstance().reference.child("items")

        // Fetch data from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(HealthInfoData::class.java)
                    if (item != null) {
                        itemList.add(item)
                    }
                }
                adapter = HealthInfoAdapter(itemList) { deletedItem ->
                    itemList.remove(deletedItem) // Remove the item locally
                    adapter.notifyDataSetChanged() // Refresh the RecyclerView
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}