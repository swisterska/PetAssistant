package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth


/**
 * Activity responsible for handling user login using Firebase Authentication.
 * It validates user credentials, performs sign-in, and navigates to the next activity upon success.
 */
class LoginActivity : BaseActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button

    /**
     * Called when the activity is first created.
     * Sets up UI components and their interactions.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {

            val intent = Intent(this, LogRegActivity::class.java)
            startActivity(intent)
        }

        // Initialize input fields and login button
        inputEmail = findViewById(R.id.emailTextEdit)
        inputPassword = findViewById(R.id.passwordTextEdit)
        loginButton = findViewById(R.id.LoginButton)

        // Set click listener for the login button
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

    }


    /**
     * Validates the login details entered by the user.
     * Ensures that email and password fields are not empty.
     *
     * @return True if both email and password are valid, otherwise False.
     */
    private fun validateLoginDetails(): Boolean {
        val email = inputEmail?.text.toString().trim { it <= ' ' }
        val password = inputPassword?.text.toString().trim { it <= ' ' }

        return when {
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                // Optionally show a success message
                true
            }
        }
    }

    /**
     * Attempts to log in the registered user using Firebase Authentication.
     * On success, navigates to the ChooseYourPetActivity.
     * On failure, displays an error message using a Snackbar.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            // Sign in with FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You are logged in successfully.", false)
                        goToChooseYourPetActivity()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Navigates to the ChooseYourPetActivity after successful login.
     * Passes the user's email as an extra in the intent.
     */
    open fun goToChooseYourPetActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.orEmpty()

        val intent = Intent(this, ChooseYourPetActivity::class.java).apply {
            putExtra("uID", email)
        }
        startActivity(intent)
    }
}