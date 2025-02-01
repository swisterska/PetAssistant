package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.firebase.FirestoreClass
import com.example.finalproject.firebase.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * RegisterActivity handles the registration process for new users.
 * It collects user details such as username, email, and password, and then registers the user with Firebase Authentication.
 */
class RegisterActivity : BaseActivity() {

    private lateinit var inputUserName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordRepeat: EditText
    private lateinit var registerButton: Button

    /**
     * Called when the activity is first created.
     * Sets up the UI elements, such as the input fields and register button.
     * Also defines the click listeners for UI elements.
     *
     * @param savedInstanceState A bundle containing any saved state from a previous session (if applicable).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Set up the "Go Back" button to navigate back to the previous activity
        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {
            startActivity(Intent(this, LogRegActivity::class.java))
        }

        // Initialize UI elements
        inputUserName = findViewById(R.id.usernameTextEdit)
        inputEmail = findViewById(R.id.emailTextEdit)
        inputPassword = findViewById(R.id.passwordTextEdit)
        inputPasswordRepeat = findViewById(R.id.confirmPasswordTextEdit)
        registerButton = findViewById(R.id.RegisterButton)

        // Set the click listener for the register button to initiate registration
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Validates the input fields to ensure that all required fields are filled correctly.
     * Also checks if the password and confirmation password match.
     *
     * @return Boolean indicating whether the registration details are valid.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            inputUserName.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_username), true)
                false
            }
            inputEmail.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }
            inputPassword.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }
            inputPasswordRepeat.text.trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_confpassword), true)
                false
            }
            inputPassword.text.toString() != inputPasswordRepeat.text.toString() -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }
            else -> true
        }
    }

    /**
     * Registers the user using Firebase Authentication.
     * If registration is successful, it saves the user data to Firestore.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val name = inputUserName.text.toString().trim()

            // Create a new user with the given email and password using Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        // Create a User object to store in Firestore
                        val user = User(
                            id = firebaseUser.uid,
                            username = name,
                            email = email,
                            pets = mutableListOf()
                        )

                        // Save user data to Firestore asynchronously
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val firestoreClass = FirestoreClass()
                                firestoreClass.registerOrUpdateUser(user)

                                // Switch back to the main thread to update UI
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@RegisterActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                                    finish() // Finish activity after successful registration
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@RegisterActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        showErrorSnackBar(task.exception?.message ?: "Registration failed", true)
                    }
                }
        }
    }
}
