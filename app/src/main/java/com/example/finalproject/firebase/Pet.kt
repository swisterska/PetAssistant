package com.example.finalproject.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter


/**
 * Enum representing the gender of a pet.
 */
enum class Gender {

    /**
     * Represents male gender.
     */
    MALE,

    /**
     * Represents female gender.
     */
    FEMALE,

    /**
     * Represents cases where the gender is not specified or unknown.
     */
    UNKNOWN // For cases where gender isn't specified
}

/**
 * Enum representing the species of a pet.
 */
enum class Species {
    AXOLOTL,
    BIRD,
    CAT,
    CHAMELEON,
    CHICKEN,
    CHINCHILLA,
    COW,
    DOG,
    FERRET,
    FISH,
    FROG,
    GECKO,
    GOAT,
    GUINEA_PIG,
    HAMSTER,
    HEDGEHOG,
    HORSE,
    IGUANA,
    LIZARD,
    MOUSE,
    PARROT,
    PIG,
    RABBIT,
    RAT,
    SHEEP,
    SNAKE,
    SPIDER,
    SQUIRREL,
    TORTOISE,
    TURTLE,
    UNKNOWN
}
/**
 * Represents a pet with various attributes such as name, species, and health details.
 */
data class Pet(
    var iconUri: String? = null,
    var id: String = "",
    var name: String = "",
    var dob: String? = null,
    var species: Species = Species.UNKNOWN,
    var breed: String = "",
    var allergies: List<String> = mutableListOf(),
    var diseases: List<String> = mutableListOf(),
    var weight: Double,
    var gender: Gender = Gender.UNKNOWN,
    var feedingTime: MutableList<String> = mutableListOf(),
    var waterTime: MutableList<String> = mutableListOf(),
    var symptoms: MutableList<String> = mutableListOf(),
    var healthNotes: String = "",
    var healthHistory: MutableList<String> = mutableListOf(),
    val ownerId: String = ""
)
{
    // No-argument constructor
    constructor() : this(
        iconUri = null,
        id = "",
        name = "",
        dob = null,
        species = Species.UNKNOWN,
        breed = "",
        allergies = mutableListOf(),
        diseases = mutableListOf(),
        weight = 0.0,
        gender = Gender.UNKNOWN,
        feedingTime = mutableListOf(),
        waterTime = mutableListOf(),
        symptoms = mutableListOf(),
        healthNotes = "",
        healthHistory = mutableListOf(),
        ownerId = ""
    )

    companion object {
        /**
         * Converts a map of Firestore document data into a Pet object.
         *
         * @param data The map containing the pet data fetched from Firestore.
         * @return A `Pet` object containing the mapped data.
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromMap(data: Map<String, Any?>): Pet {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Example format: 2023-01-01


            return Pet(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                species = (data["species"] as? String)?.let {
                    try {
                        Species.valueOf(it.uppercase()) // Convert species name to uppercase before enum lookup
                    } catch (e: IllegalArgumentException) {
                        Species.UNKNOWN // Default value if species does not match
                    }
                } ?: Species.UNKNOWN, // Ensure a non-null fallback

                feedingTime = (data["feedingTime"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                waterTime = (data["waterTime"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                healthNotes = data["healthNotes"] as? String ?: "",
                healthHistory = (data["healthHistory"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                ownerId = data["ownerId"] as? String ?: "",
                iconUri = data["iconUri"] as? String,
                dob = data["dob"] as? String,
                breed = data["breed"] as? String ?: "",
                allergies = (data["allergies"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                diseases = (data["diseases"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,

                gender = (data["gender"] as? String)?.let {
                    try {
                        Gender.valueOf(it.uppercase()) // Convert gender string to uppercase for enum lookup
                    } catch (e: IllegalArgumentException) {
                        Gender.UNKNOWN // Default value if gender does not match
                    }
                } ?: Gender.UNKNOWN
            )

        }
    }
}


