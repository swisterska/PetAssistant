package com.example.finalproject.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    suspend fun registerOrUpdateUser(user: User) {
        try {
            mFireStore.collection("users").document(user.id).set(user).await()
            Log.d("FirestoreClass", "User successfully registered/updated.")
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error saving user data: ${e.message}", e)
            throw Exception("Error saving user data: ${e.message}")
        }
    }

    suspend fun loadUserData(userId: String): Map<String, Any>? {
        return try {
            val documentSnapshot = mFireStore.collection("users").document(userId).get().await()
            documentSnapshot.data
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error loading user data: ${e.message}", e)
            throw Exception("Error loading user data: ${e.message}")
        }
    }

    suspend fun updateUserData(userId: String, updatedData: Map<String, Any?>) {
        try {
            val filteredData = updatedData.filterValues { it != null && !(it is String && it.isBlank()) }
            if (filteredData.isNotEmpty()) {
                mFireStore.collection("users").document(userId).update(filteredData).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error updating user data: ${e.message}", e)
            throw Exception("Error updating user data: ${e.message}")
        }
    }
}