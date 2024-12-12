package com.example.finalproject.firebase

class User( val id: String="",
            val username: String?=null,
            val email: String="",
)

{
    companion object {
        /**
         * Converts a map of Firestore document data into a User object.
         *
         * This method ensures type safety by checking the type of each field in the map before
         * assigning it to the corresponding property in the `User` object. If a field is missing
         * or has the wrong type, a default value is used instead.
         *
         * @param data The map containing the user data fetched from Firestore.
         * @return A `User` object containing the mapped data.
         *
         */
        fun fromMap(data: Map<String, Any?>): User {
            return User(
                // Retrieve the "id" field as a string, or use an empty string if it's missing
                id = data["id"] as? String ?: "",

                // Retrieve the "name" field as a nullable string
                username = data["username"] as? String,


                // Retrieve the "email" field as a string, or use an empty string if it's missing
                email = data["email"] as? String ?: "")
        }
    }
}