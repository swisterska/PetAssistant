package com.example.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.finalproject.databinding.ActivityVetsNearby2Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject

/**
 * This activity displays a Google Map with the user's current location and nearby veterinary clinics.
 * The user can also see a list of nearby veterinarians in a ListView.
 */
class VetsNearbyActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityVetsNearby2Binding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    companion object {
        // Request code for location permission
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVetsNearby2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve petId passed from previous activity
        val petId = intent.getStringExtra("petId")

        // Set up the go back button to navigate back to the main page
        val goBackButton = findViewById<Button>(R.id.btn_find_vets_go_back)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        // Initialize the location client to get user's location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Called when the Google Map is ready to be used.
     * This method sets up the map and handles the user's current location.
     *
     * @param googleMap The Google Map instance to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
    }

    /**
     * Sets up the map by requesting the user's location and enabling location features.
     * It also calls the method to find nearby places (veterinary clinics) once the location is available.
     */
    private fun setUpMap() {
        // Check for location permission, if not granted, request permission
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        // Enable location layer on the map
        mMap.isMyLocationEnabled = true

        // Fetch the user's last known location
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

                Log.d("VetsNearbyActivity", "User location: $currentLatLng")
                // Call function to find nearby veterinary clinics
                findNearbyPlaces("veterinary_care", 50000)
            } else {
                Log.e("VetsNearbyActivity", "Could not retrieve location.")
            }
        }.addOnFailureListener { e ->
            Log.e("VetsNearbyActivity", "Failed to get location: ${e.message}")
        }
    }

    /**
     * Finds nearby places of a given type (e.g., "veterinary_care") within a specified radius.
     * The method makes a request to the Google Places API to find the places.
     *
     * @param type The type of places to search for (e.g., "veterinary_care").
     * @param radius The radius (in meters) to search around the user's location.
     */
    private fun findNearbyPlaces(type: String, radius: Int) {
        val location = lastKnownLocation
        if (location == null) {
            Log.e("VetsNearbyActivity", "User location is null, cannot find places.")
            return
        }

        val apiKey = getString(R.string.google_maps_key)
        val locationString = "${location.latitude},${location.longitude}"
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=$locationString&radius=$radius&type=$type&key=$apiKey"

        Log.d("API Request", "Requesting: $url")

        // Send a request to Google Places API using Volley library
        val request = object : StringRequest(Method.GET, url,
            Response.Listener { response ->
                Log.d("API Response", response)
                try {
                    // Parse the JSON response from the Places API
                    val jsonObject = JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    mMap.clear() // Clear any previous markers on the map

                    if (results.length() == 0) {
                        Log.w("VetsNearbyActivity", "No nearby places found.")
                    }

                    val vetList = mutableListOf<Vet>()

                    // Parse each place and add it to the map and list
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val location = place.getJSONObject("geometry").getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        val placeName = place.getString("name")

                        // Add the place as a marker on the map
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(placeName)
                        )

                        // Add the vet information to the list
                        vetList.add(Vet(placeName, lat, lng))
                    }

                    // Pass the user's location to the VetAdapter to calculate distances
                    val adapter = VetAdapter(this, vetList, location)
                    val vetListView: ListView = findViewById(R.id.vet_list_view)
                    vetListView.adapter = adapter

                } catch (e: JSONException) {
                    Log.e("VetsNearbyActivity", "JSON parsing error: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("VetsNearbyActivity", "Error fetching places: ${error.message}")
            }) {}

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
    }



    /**
     * Handles the result of the location permission request.
     * If permission is granted, the map is set up. Otherwise, an error message is logged.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpMap()
            } else {
                Log.e("VetsNearbyActivity", "Location permission denied.")
            }
        }
    }
}
