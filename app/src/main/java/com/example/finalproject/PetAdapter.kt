package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.firebase.Pet

class PetAdapter(private val pets: List<Pet>, private val onPetClicked: (Pet) -> Unit) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_pet_adapter, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        holder.bind(pet)
        holder.itemView.setOnClickListener {
            onPetClicked(pet)
        }
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        private val petSpeciesTextView: TextView = itemView.findViewById(R.id.petSpeciesTextView)

        fun bind(pet: Pet) {
            petNameTextView.text = pet.name
            petSpeciesTextView.text = pet.species.toString()
        }
    }
}
