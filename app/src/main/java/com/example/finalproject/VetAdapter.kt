package com.example.finalproject

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Custom adapter to display a list of veterinarians in the VetNearbyActivity.
 * It handles displaying the vet's name and opens Google Maps for navigation when clicked.
 *
 * @param context The context in which the adapter is being used, typically the activity.
 * @param vetList A list of Vet objects representing the veterinarians to be displayed.
 */
class VetAdapter(
    private val context: VetsNearbyActivity,
    private val vetList: List<Vet>  // Vet is a data class that holds name, lat, lng
) : ArrayAdapter<Vet>(context, R.layout.vet_list_item, vetList) {

    /**
     * Returns a view for a single list item in the ListView.
     * Binds data from the Vet object to the view.
     *
     * @param position The position of the item within the adapter's data set.
     * @param convertView A recycled view to be reused (if available).
     * @param parent The parent view group that this view will eventually be attached to.
     * @return The view for the list item at the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // If convertView is null, inflate a new view
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.vet_list_item, parent, false)

        // Get the current Vet item based on the position
        val vet = getItem(position)

        // Get reference to the TextView where the vet's name will be displayed
        val vetNameTextView: TextView = view.findViewById(R.id.vet_name)

        // Set the vet's name to the TextView
        vetNameTextView.text = vet?.name

        // Set a click listener to open Google Maps navigation when a list item is clicked
        view.setOnClickListener {
            // Create a URI for Google Maps with the vet's latitude and longitude for navigation
            val gmmIntentUri = Uri.parse("google.navigation:q=${vet?.latitude},${vet?.longitude}")
            // Create an intent to open Google Maps with the specified URI
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            // Start the map activity
            context.startActivity(mapIntent)
        }

        return view
    }
}

/**
 * Data class representing a veterinarian.
 * Holds the name and location (latitude and longitude) of the vet.
 *
 * @param name The name of the veterinarian.
 * @param latitude The latitude of the veterinarian's location.
 * @param longitude The longitude of the veterinarian's location.
 */
data class Vet(
    val name: String,
    val latitude: Double,
    val longitude: Double
)


