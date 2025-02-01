package com.example.finalproject.firebase

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * A data class representing a user in the application.
 *
 * @property id The unique identifier for the user. Defaults to an empty string.
 * @property username The username of the user. This is nullable, as it may not always be provided.
 * @property email The user's email address. This is required and cannot be null.
 * @property pets A list of pets associated with the user. Defaults to an empty list if no pets are provided.
 */
data class User(
    val id: String = "",
    val username: String? = null,
    val email: String = "",
    val pets: MutableList<Pet> = mutableListOf()
) {
    companion object {
        /**
         * Converts a map of Firestore document data into a User object.
         *
         * @param data The map containing the user data fetched from Firestore.
         * @return A `User` object containing the mapped data.
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromMap(data: Map<String, Any?>): User {
            return User(
                id = data["id"] as? String ?: "",
                username = data["username"] as? String,
                email = data["email"] as? String ?: "",
                pets = (data["pets"] as? List<Map<String, Any?>>)?.mapNotNull {
                    Pet.fromMap(it)
                }?.toMutableList() ?: mutableListOf() // Ensure pets is mutable
            )
        }
    }
}