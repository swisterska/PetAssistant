package com.example.finalproject.googleMaps

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("results") val results: List<PlaceResult>
)

data class PlaceResult(
    @SerializedName("name") val name: String,
    @SerializedName("vicinity") val vicinity: String,
    @SerializedName("geometry") val geometry: Geometry
)

data class Geometry(
    @SerializedName("location") val location: LocationData
)

data class LocationData(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)