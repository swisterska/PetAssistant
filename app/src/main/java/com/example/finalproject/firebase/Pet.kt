package com.example.finalproject.firebase

import java.time.LocalDate

enum class Gender {
    MALE,
    FEMALE,
    UNKNOWN // For cases where gender isn't specified
}

data class Pet(
    var iconUri: String? = null,
    val id: String = "",
    var name: String = "",
    var dob: LocalDate? = null,
    var species: String = "",
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
        fun fromMap(data: Map<String, Any?>): Pet {
            return Pet(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                species = data["species"] as? String ?: "",
                feedingTime = (data["feedingTime"] as? List<String>)?.toMutableList()
                    ?: mutableListOf(),
                waterTime = (data["waterTime"] as? List<String>)?.toMutableList()
                    ?: mutableListOf(),
                healthNotes = data["healthNotes"] as? String ?: "",
                healthHistory = (data["healthHistory"] as? List<String>)?.toMutableList()
                    ?: mutableListOf(),
                ownerId = data["ownerId"] as? String ?: "",
                iconUri = TODO(),
                dob = TODO(),
                breed = TODO(),
                allergies = TODO(),
                diseases = TODO(),
                weight = TODO(),
                city = TODO(),
                gender = TODO()
            )
        }
    }
}