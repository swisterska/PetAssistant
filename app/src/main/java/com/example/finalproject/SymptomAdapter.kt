package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SymptomAdapter(
    private val symptomList: MutableList<SymptomData>,
    private val userId: String,
    private val petId: String,
    private val onItemDeleted: (SymptomData) -> Unit
) : RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_symptom, parent, false)
        return SymptomViewHolder(view)
    }

    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.bind(symptomList[position])
    }

    override fun getItemCount(): Int = symptomList.size

    inner class SymptomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symptomDate: TextView = itemView.findViewById(R.id.symptomDate)
        private val symptomTitle: TextView = itemView.findViewById(R.id.symptomTitle)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(symptom: SymptomData) {
            symptomDate.text = "Date: ${symptom.timestamp}"
            symptomTitle.text = "Symptom: ${symptom.symptom}"

            deleteButton.setOnClickListener {
                symptom.id?.let { symptomId ->
                    db.collection("users")
                        .document(userId)
                        .collection("pets")
                        .document(petId)
                        .collection("symptoms")
                        .document(symptomId)
                        .delete()
                        .addOnSuccessListener {
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                symptomList.removeAt(position)
                                notifyItemRemoved(position)
                                onItemDeleted(symptom)
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
        }
    }
}
