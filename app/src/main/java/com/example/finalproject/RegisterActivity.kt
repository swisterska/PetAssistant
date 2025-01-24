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

class RegisterActivity : BaseActivity() {

    private lateinit var inputUserName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordRepeat: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

        registerButton.setOnClickListener {
            registerUser()
        }
    }

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

    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val name = inputUserName.text.toString().trim()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        val user = User(
                            id = firebaseUser.uid,
                            username = name,
                            email = email,
                            pets = emptyList()
                        )

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val firestoreClass = FirestoreClass()
                                firestoreClass.registerOrUpdateUser(user)

                                // Switch back to the main thread to update UI
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@RegisterActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                                    finish()
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
