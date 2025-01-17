package com.example.finalproject.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class Gender {
    MALE,
    FEMALE,
    UNKNOWN // For cases where gender isn't specified
}

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

data class Pet(
    var iconUri: String? = null,
    var id: String = "",
    var name: String = "",
    var dob: LocalDate? = null,
    var species: Species = Species.UNKNOWN,
    var breed: String = "",
    var allergies: MutableList<String> = mutableListOf(),
    var diseases: MutableList<String> = mutableListOf(),
    var weight: Double,
    var city: String = "",
    var gender: Gender = Gender.UNKNOWN,
    var feedingTime: MutableList<String> = mutableListOf(),
    var waterTime: MutableList<String> = mutableListOf(),
    var healthNotes: String = "",
    var healthHistory: MutableList<String> = mutableListOf(),
    val ownerId: String = ""
) {
    fun addFeedingTime(time: String) {
        feedingTime.add(time)
    }

    fun addWaterTime(time: String) {
        waterTime.add(time)
    }

    fun addHealthNote(note: String) {
        healthNotes += "$note\n"
        healthHistory.add(note)
    }

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

                dob = (data["dob"] as? String)?.let {
                    try {
                        LocalDate.parse(it, formatter) // Use a predefined formatter for date parsing
                    } catch (e: DateTimeParseException) {
                        null // Return null if parsing fails
                    }
                },

                breed = data["breed"] as? String ?: "",
                allergies = (data["allergies"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                diseases = (data["diseases"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,
                city = data["city"] as? String ?: "",

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
