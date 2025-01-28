package com.example.finalproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.firebase.Pet

class PetAdapter(private val pets: List<Pet>, private val onPetClick: (Pet) -> Unit) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
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
        private val petIcon: ImageView = itemView.findViewById(R.id.petIconImageView)
        private val petName: TextView = itemView.findViewById(R.id.petNameTextView)
        private val petSpecies: TextView = itemView.findViewById(R.id.petSpeciesTextView)

        fun bind(pet: Pet) {
            petName.text = pet.name ?: "Unknown Pet"
            petSpecies.text = (pet.species ?: "Unknown Species").toString()

            // Load image using Glide, or set default
            if (!pet.iconUri.isNullOrEmpty()) {
                Log.d("PetAdapter", "Loading image for ${pet.name} from ${pet.iconUri}")
                Glide.with(itemView.context)
                    .load(pet.iconUri)
                    .placeholder(R.drawable.dogicon)
                    .error(R.drawable.dogicon)
                    .into(petIcon)
            } else {
                Log.d("PetAdapter", "Using default image for ${pet.name}")
                petIcon.setImageResource(R.drawable.dogicon)
            }

            // Click listener for pet selection
            itemView.setOnClickListener {
                Log.d("PetAdapter", "Pet clicked: ${pet.name}, ID: ${pet.id}")
                onPetClick(pet)
            }
        }
    }
}