package com.example.finalproject


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase


class HealthInfoAdapter(private val itemList: List<HealthInfoData>, private val onItemDeleted: (HealthInfoData) -> Unit) : RecyclerView.Adapter<HealthInfoAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.itemDate)
        val text: TextView = view.findViewById(R.id.itemText)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_health_info_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]



        holder.date.text = item.date
        holder.text.text = item.text

        // Delete button functionality
        holder.btnDelete.setOnClickListener {
            val database = FirebaseDatabase.getInstance().reference
            database.child("items").child(item.id).removeValue()
            onItemDeleted(item) // Notify about the deletion
        }
    }

    override fun getItemCount(): Int = itemList.size
}