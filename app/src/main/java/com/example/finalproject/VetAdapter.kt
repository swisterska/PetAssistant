package com.example.finalproject

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class VetAdapter(
    private val context: VetsNearbyActivity,
    private val vetList: List<Vet>  // Vet is a data class that holds name, lat, lng
) : ArrayAdapter<Vet>(context, R.layout.vet_list_item, vetList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.vet_list_item, parent, false)
        val vet = getItem(position)

        val vetNameTextView: TextView = view.findViewById(R.id.vet_name)
        vetNameTextView.text = vet?.name

        view.setOnClickListener {
            // Open Google Maps when the list item is clicked
            val gmmIntentUri = Uri.parse("google.navigation:q=${vet?.latitude},${vet?.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        }

        return view
    }
}


data class Vet(
    val name: String,
    val latitude: Double,
    val longitude: Double
)


