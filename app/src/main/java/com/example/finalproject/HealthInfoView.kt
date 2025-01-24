package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

/**
 * Activity for displaying a list of health information items using a RecyclerView.
 * Data is fetched from a Firebase Realtime Database node named "items".
 */
class HealthInfoView : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthInfoAdapter
    private lateinit var database: DatabaseReference
    private var itemList = mutableListOf<HealthInfoData>()

    /**
     * Initializes the activity and sets up the RecyclerView and Firebase database listener.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
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
                    itemList.remove(deletedItem)
                    adapter.notifyDataSetChanged()
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
            }
        })

    }
}