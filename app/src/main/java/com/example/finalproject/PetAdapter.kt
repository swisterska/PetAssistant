package com.example.finalproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.firebase.Pet

class PetAdapter(private val pets: List<Pet>, private val onPetClick: (Pet) -> Unit) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        // Inflate the new layout: item_choose_pet_recycler.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choose_pet_recycler, parent, false)
        Log.d("PetAdapter", "ViewHolder created")
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        Log.d("PetAdapter", "Binding pet: ${pet.name}, ID: ${pet.id}")
        holder.bind(pet)
    }

    override fun getItemCount(): Int {
        Log.d("PetAdapter", "Item count: ${pets.size}")
        return pets.size
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petIcon: ImageButton = itemView.findViewById(R.id.petIconImageView)
        private val petName: TextView = itemView.findViewById(R.id.petNameTextView)
        private val petSpecies: TextView = itemView.findViewById(R.id.petSpeciesTextView)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton) // Add this

        fun bind(pet: Pet) {
            petName.text = pet.name ?: "Unknown Pet"
            petSpecies.text = (pet.species ?: "Unknown Species").toString()

            // Load the selected icon from Firestore and set it to ImageButton
            val defaultIcon = R.drawable.defaulticon  // Default icon

            // Use iconUri from Firestore to determine the correct icon
            val iconResId = when (pet.iconUri) {
                "dogicon" -> R.drawable.dogicon
                "caticon" -> R.drawable.caticon
                "rabbiticon" -> R.drawable.rabbiticon
                "snakeicon" -> R.drawable.snakeicon
                else -> defaultIcon // Fallback to default if no match
            }

            petIcon.setImageResource(iconResId)  // Set the correct icon

            // Click listener for pet selection
            itemView.setOnClickListener {
                onPetClick(pet)
            }

            // Click listener for edit button
            editButton.setOnClickListener {
                val context = itemView.context
                if (context is ChooseYourPetActivity) {
                    context.showEditPetDialog(pet)
                }
            }
        }
    }
}
