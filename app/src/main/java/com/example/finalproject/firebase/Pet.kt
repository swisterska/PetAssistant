package com.example.finalproject.firebase

data class Pet(
    val id: String = "",
    var name: String = "",
    var species: String = "",
    var age: Int = 0,
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
                age = (data["age"] as? Number)?.toInt() ?: 0,
                feedingTime = (data["feedingTime"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                waterTime = (data["waterTime"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                healthNotes = data["healthNotes"] as? String ?: "",
                healthHistory = (data["healthHistory"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                ownerId = data["ownerId"] as? String ?: ""
            )
        }
    }
}

