package com.example.finalproject

/**
 * Data class representing a health information item.
 *
 * @property id A unique identifier for the health info item.
 * @property text The descriptive text or notes for the health info item.
 * @property date The date associated with the health info item (e.g., the date the information was added or recorded).
 */
data class HealthInfoData(
    val id: String = "",
    val symptom: String = "", // Ensure this matches the Firebase "symptom" property
    val date: String = ""
)