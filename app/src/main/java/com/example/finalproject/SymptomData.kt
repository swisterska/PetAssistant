package com.example.finalproject

import com.google.firebase.database.Exclude

/**
 * Data class for symptoms stored in Firestore.
 *
 * @param id The unique ID of the symptom document in Firestore.
 * @param title The name of the symptom (e.g., "Coughing").
 * @param description Additional details about the symptom.
 * @param timestamp The timestamp when the symptom was recorded.
 */
data class SymptomData(
    @Exclude val id: String? = null,
    val symptom: String = "",
    val description: String = "",
    val timestamp: String = ""
)
