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
    val pets: List<Pet> = emptyList() // Default to an empty list
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
                pets = (data["pets"] as? List<Map<String, Any?>>)?.map {
                    Pet.fromMap(it)
                } ?: listOf()
            )
        }

        /**
         * Adds a new pet to the user's pet list and returns an updated User object.
         *
         * @param user The user object to which the pet will be added.
         * @param pet The new pet to be added.
         * @return A new User object with the updated list of pets.
         */
        fun addPet(user: User, pet: Pet): User {
            val updatedPets = user.pets.toMutableList().apply { add(pet) }
            return user.copy(pets = updatedPets)
        }

        /**
         * Updates an existing pet's data in the user's pet list and returns an updated User object.
         *
         * @param user The user object whose pet will be updated.
         * @param updatedPet The updated pet object.
         * @return A new User object with the modified pet list.
         */
        fun updatePet(user: User, updatedPet: Pet): User {
            val updatedPets = user.pets.map { if (it.id == updatedPet.id) updatedPet else it }
            return user.copy(pets = updatedPets)
        }

        /**
         * Removes a pet from the user's pet list and returns an updated User object.
         *
         * @param user The user object from which the pet will be removed.
         * @param petId The ID of the pet to be removed.
         * @return A new User object with the updated list of pets.
         */
        fun removePet(user: User, petId: String): User {
            val updatedPets = user.pets.filter { it.id != petId }
            return user.copy(pets = updatedPets)
        }
    }
}