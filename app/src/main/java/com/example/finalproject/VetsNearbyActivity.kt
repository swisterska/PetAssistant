package com.example.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

class VetsNearbyActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityVetsNearby2Binding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVetsNearby2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val petId = intent.getStringExtra("petId")

        val goBackButton = findViewById<Button>(R.id.btn_find_vets_go_back)
        goBackButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
    }

    private fun setUpMap() {
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

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

                Log.d("VetsNearbyActivity", "User location: $currentLatLng")
                findNearbyPlaces("veterinary_care", 500000)
            } else {
                Log.e("VetsNearbyActivity", "Could not retrieve location.")
            }
        }.addOnFailureListener { e ->
            Log.e("VetsNearbyActivity", "Failed to get location: ${e.message}")
        }
    }

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

        val request = object : StringRequest(Method.GET, url,
            Response.Listener { response ->
                Log.d("API Response", response)
                try {
                    val jsonObject = JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    mMap.clear()

                    if (results.length() == 0) {
                        Log.w("VetsNearbyActivity", "No nearby places found.")
                    }

                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val location = place.getJSONObject("geometry").getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        val placeName = place.getString("name")

                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(placeName)
                        )
                    }
                } catch (e: JSONException) {
                    Log.e("VetsNearbyActivity", "JSON parsing error: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("VetsNearbyActivity", "Error fetching places: ${error.message}")
            }) {}

        Volley.newRequestQueue(this).add(request)
    }

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
