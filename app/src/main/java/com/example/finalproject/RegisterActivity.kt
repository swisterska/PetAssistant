package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.example.finalproject.firebase.FirestoreClass
import com.example.finalproject.firebase.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class RegisterActivity : BaseActivity() {

    private var inputUserName: EditText? = null
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var inputPasswordRepeat: EditText? = null
    private var registerButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButton)
        returnButton.setOnClickListener {

            val intent = Intent(this, LogRegActivity::class.java)
            startActivity(intent)
        }


        // Initialize input fields and the registration button
        inputUserName = findViewById(R.id.usernameTextEdit)
        inputEmail = findViewById(R.id.emailTextEdit)
        inputPassword = findViewById(R.id.passwordTextEdit)
        inputPasswordRepeat = findViewById(R.id.confirmPasswordTextEdit)
        registerButton = findViewById(R.id.RegisterButton)


        // Set a click listener for the registration button
        registerButton?.setOnClickListener {
            registerUser()
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {

            inputUserName?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_username), true)
                false
            }

            inputEmail?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }


            inputPassword?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }

            inputPasswordRepeat?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_confpassword), true)
                false
            }

            inputPassword?.text.toString().trim { it <= ' ' } != inputPasswordRepeat?.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }

            else -> true
        }
    }



    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }
            val name = inputUserName?.text.toString().trim { it <= ' ' }

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
                            email = email
                        )


                        lifecycleScope.launch {
                            try {
                                val firestoreClass = FirestoreClass()
                                firestoreClass.registerOrUpdateUser(user)
                                Toast.makeText(this@RegisterActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@RegisterActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        //FirebaseAuth.getInstance().signOut()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
    fun userRegistrationSuccess() {
        Toast.makeText(
            this@RegisterActivity,
            getString(R.string.register_success),
            Toast.LENGTH_LONG
        ).show()
    }

}