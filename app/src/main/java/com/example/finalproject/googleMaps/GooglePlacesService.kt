package com.example.finalproject.googleMaps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesService {
    @GET("place/nearbysearch/json")
    fun getNearbyVets(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String = "veterinary",
        @Query("keyword") keyword: String = "veterinarian", // Helps Google return relevant results
        @Query("key") apiKey: String
    ): Call<PlacesResponse>
}