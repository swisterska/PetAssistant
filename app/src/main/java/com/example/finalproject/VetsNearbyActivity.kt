package com.example.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.finalproject.googleMaps.GooglePlacesService
import com.example.finalproject.googleMaps.NearbyVet
import com.example.finalproject.googleMaps.PlacesResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class VetsNearbyActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var vetListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val vetsList = mutableListOf<NearbyVet>()

    private val API_KEY = ""  // Ensure this is correct and valid
    private val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val TAG = "VetsNearbyActivity"  // For logging purposes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vets_nearby)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButtonVetsNearby)
        returnButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        vetListView = findViewById(R.id.vetListView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d(TAG, "Location retrieved: Lat: ${location.latitude}, Lng: ${location.longitude}")
                fetchNearbyVets(location.latitude, location.longitude)
            } else {
                Log.e(TAG, "Location is null")
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchNearbyVets(lat: Double, lng: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GooglePlacesService::class.java)

        // Use a broader keyword search (veterinary or animal hospital) instead of specific type
        val call = service.getNearbyVets(
            location = "$lat,$lng",
            radius = 50000,  // Increased radius to 50km
            keyword = "veterinary",  // Searching by keyword instead of place type
            apiKey = API_KEY
        )

        call.enqueue(object : Callback<PlacesResponse> {
            override fun onResponse(call: Call<PlacesResponse>, response: Response<PlacesResponse>) {
                if (response.isSuccessful) {
                    val places = response.body()?.results ?: emptyList()
                    Log.d(TAG, "API call success, found ${places.size} places.")
                    if (places.isEmpty()) {
                        Toast.makeText(this@VetsNearbyActivity, "No nearby vets found.", Toast.LENGTH_SHORT).show()
                    }

                    vetsList.clear()
                    places.forEach { place ->
                        vetsList.add(
                            NearbyVet(
                                name = place.name,
                                address = place.vicinity,
                                latitude = place.geometry.location.lat,
                                longitude = place.geometry.location.lng
                            )
                        )
                    }
                    updateListView(lat, lng)
                } else {
                    Log.e(TAG, "API response failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@VetsNearbyActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                Log.e(TAG, "API call failure: ${t.message}")
                Toast.makeText(this@VetsNearbyActivity, "Error fetching vets!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateListView(currentLat: Double, currentLng: Double) {
        if (vetsList.isEmpty()) {
            Log.d(TAG, "No vets found to display")
            return
        }

        vetsList.sortBy { vet ->
            val results = FloatArray(1)
            Location.distanceBetween(currentLat, currentLng, vet.latitude, vet.longitude, results)
            results[0] // Distance in meters
        }

        val vetNames = vetsList.map { "${it.name} - ${it.address}" }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, vetNames)
        vetListView.adapter = adapter

        // Open Google Maps for directions when an item is clicked
        vetListView.setOnItemClickListener { _, _, position, _ ->
            val vet = vetsList[position]
            val gmmIntentUri = Uri.parse("google.navigation:q=${vet.latitude},${vet.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }
}
