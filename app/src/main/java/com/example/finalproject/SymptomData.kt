package com.example.finalproject

/**
 * Data class for symptoms stored in Firebase.
 *
 * @param id The unique ID of the symptom.
 * @param symptom The symptom title.
 * @param description The description of the symptom.
 * @param date The timestamp when the symptom was added.
 * @param userId The ID of the user who added the symptom.
 * @param petId The ID of the pet associated with the symptom.
 */
data class SymptomData(
    val id: String? = null,
    val symptom: String = "",
    val description: String = "",
    val date: String = "",
    val userId: String = "",
    val petId: String = ""
)
