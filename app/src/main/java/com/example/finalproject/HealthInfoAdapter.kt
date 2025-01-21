package com.example.finalproject


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

/**
 * Adapter class for displaying a list of health information in a RecyclerView.
 *
 * @param itemList The list of health information items to display.
 * @param onItemDeleted A callback function triggered when an item is deleted.
 */
class HealthInfoAdapter(private val itemList: List<HealthInfoData>, private val onItemDeleted: (HealthInfoData) -> Unit) : RecyclerView.Adapter<HealthInfoAdapter.ItemViewHolder>() {
    /**
     * ViewHolder class for caching views within each item of the RecyclerView.
     *
     * @param view The layout view for a single item.
     */
    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.itemDate)
        val text: TextView = view.findViewById(R.id.itemText)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    /**
     * Creates and inflates the view for each RecyclerView item.
     *
     * @param parent The parent ViewGroup into which the view will be added.
     * @param viewType The view type of the new View.
     * @return An instance of [ItemViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_health_info_view, parent, false)
        return ItemViewHolder(view)
    }

    /**
     * Binds data to the views in a ViewHolder.
     *
     * @param holder The [ItemViewHolder] containing the views to bind data to.
     * @param position The position of the current item in the dataset.
     */
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

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The size of the [itemList].
     */
    override fun getItemCount(): Int = itemList.size
}