package com.example.finalproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.firebase.Pet

/**
 * PetAdapter is a custom RecyclerView adapter to display a list of pets.
 * Each pet has a name, species, and an icon.
 * The adapter also supports pet selection and pet editing functionality.
 *
 * @param pets List of Pet objects to be displayed in the RecyclerView.
 * @param onPetClick Lambda function that is triggered when a pet is clicked.
 */
class PetAdapter(private val pets: List<Pet>, private val onPetClick: (Pet) -> Unit) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    /**
     * Called when the RecyclerView needs a new ViewHolder.
     * Inflates the layout for each pet item and creates a new PetViewHolder.
     *
     * @param parent The parent ViewGroup that this new item view will be attached to.
     * @param viewType The view type for the new item (not used in this implementation).
     * @return A new PetViewHolder containing the inflated view for a pet item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {

        // Inflate the new layout: item_choose_pet_recycler.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choose_pet_recycler, parent, false)
        Log.d("PetAdapter", "ViewHolder created")
        return PetViewHolder(view)
    }

    /**
     * Called to bind data to the ViewHolder for a specific pet.
     * This method is called when an item needs to be displayed on the screen.
     *
     * @param holder The ViewHolder containing the views for a pet item.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        Log.d("PetAdapter", "Binding pet: ${pet.name}, ID: ${pet.id}")
        holder.bind(pet)
    }

    /**
     * Returns the number of items in the pet list.
     *
     * @return The size of the pets list.
     */

    override fun getItemCount(): Int {
        Log.d("PetAdapter", "Item count: ${pets.size}")
        return pets.size
    }

    /**
     * ViewHolder class that holds references to the views for a pet item.
     * The views include an icon, name, species, and an edit button.
     */

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petIcon: ImageButton = itemView.findViewById(R.id.petIconImageView)
        private val petName: TextView = itemView.findViewById(R.id.petNameTextView)
        private val petSpecies: TextView = itemView.findViewById(R.id.petSpeciesTextView)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton) // Add this

        /**
         * Binds the pet data (name, species, and icon) to the corresponding views.
         *
         * @param pet The pet object containing data to be displayed.
         */
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
