package com.example.finalproject.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * FirestoreClass is responsible for handling interactions with Firestore.
 * It provides methods to register or update user data in the Firestore database.
 */
class FirestoreClass {

    // Instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance() // allows you to interact with the Firestore database

    /**
     * Registers a new user or updates an existing user's data in Firestore.
     *
     * @param user The User object containing user details to be saved.
     * @throws Exception If there is an error while saving user data.
     */

    suspend fun registerOrUpdateUser(user: User) { //suspend function, means it is asynchronous and can be used with coroutines
        try {
            mFireStore.collection("users").document(user.id).set(user).await() //saves or updates the user's data in Firestore
            Log.d("FirestoreClass", "User successfully registered/updated.")
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error saving user data: ${e.message}", e)
            throw Exception("Error saving user data: ${e.message}")
        }
    }
}