package com.example.finalproject

import com.google.firebase.database.Exclude

/**
 * Data class for symptoms stored in Firestore.
 *
 * @param id The unique ID of the symptom document in Firestore.
 * @param name The name of the symptom (e.g., "Coughing").
 * @param description Additional details about the symptom.
 * @param timestamp The timestamp when the symptom was recorded.
 */
data class SymptomData(
    @Exclude val id: String? = null,  // Firestore auto-generated ID
    val symptom: String = "",  // Name of the symptom (e.g., "Coughing", "Vomiting")
    val timestamp: String = ""  // Date and time in "yyyy-MM-dd HH:mm" format
)