package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Adapter class for displaying a list of symptoms in a RecyclerView.
 * Handles binding symptom data to the RecyclerView items and deleting items.
 *
 * @param symptomList A mutable list of SymptomData to display in the RecyclerView.
 * @param userId The ID of the current user, used to locate the user's document in Firestore.
 * @param petId The ID of the pet associated with the symptoms, used to locate the pet's symptoms in Firestore.
 * @param onItemDeleted A callback function that is invoked when an item is deleted from the list.
 * @param onItemClick A callback function that is invoked when an item is clicked (for editing).
 */
class SymptomAdapter(
    private val symptomList: MutableList<SymptomData>,
    private val userId: String,
    private val petId: String,
    private val onItemDeleted: (SymptomData) -> Unit,
    private val onItemClick: (SymptomData) -> Unit
) : RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder>() {

    // Firestore instance to perform database operations
    private val db = FirebaseFirestore.getInstance()

    /**
     * Creates a new view holder to represent a single symptom item.
     *
     * @param parent The parent ViewGroup (RecyclerView).
     * @param viewType The view type of the item (used for different item layouts).
     * @return A new SymptomViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_symptom, parent, false)
        return SymptomViewHolder(view)
    }

    /**
     * Binds data from a symptom to the corresponding view holder item.
     *
     * @param holder The SymptomViewHolder that should bind the data.
     * @param position The position of the symptom item in the list.
     */
    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.bind(symptomList[position])
    }

    /**
     * Returns the total number of symptoms in the list.
     *
     * @return The size of the symptom list.
     */
    override fun getItemCount(): Int = symptomList.size

    /**
     * ViewHolder class for binding data to each item in the RecyclerView.
     */
    inner class SymptomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symptomDate: TextView = itemView.findViewById(R.id.symptomDate)
        private val symptomTitle: TextView = itemView.findViewById(R.id.symptomTitle)
        private val symptomDescription: TextView = itemView.findViewById(R.id.symptomDescription)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        /**
         * Binds a SymptomData object to the views in the item layout.
         *
         * @param symptom The SymptomData object containing the symptom details.
         */
        fun bind(symptom: SymptomData) {
            symptomDate.text = "Date: ${symptom.timestamp}"
            symptomTitle.text = "Symptom: ${symptom.symptom}"

            // Handle click event to open the symptom for editing
            itemView.setOnClickListener {
                onItemClick(symptom) // Open edit dialog when clicked
            }

            // Handle the delete button click to delete the symptom from Firestore
            deleteButton.setOnClickListener {
                symptom.id?.let { symptomId ->
                    // Delete the symptom document from Firestore
                    db.collection("users")
                        .document(userId)
                        .collection("pets")
                        .document(petId)
                        .collection("symptoms")
                        .document(symptomId)
                        .delete()
                        .addOnSuccessListener {
                            onItemDeleted(symptom) // Remove item from UI
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
        }
    }
}