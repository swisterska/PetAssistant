package com.example.finalproject


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChooseYourPetActivity : AppCompatActivity() {
    /**
     * This activity allows users to choose a pet or navigate to another activity to add a new pet.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * Called when the activity is starting. Setting the content view and initializing UI components.
         *
         * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
         *                           this contains the most recent data supplied in onSaveInstanceState.
         *                           Otherwise, it is null.
         */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_pet)

        val addnewpet = findViewById<ImageButton>(R.id.AddNewPetButton)
        addnewpet.setOnClickListener {
            val intent = Intent(this, RegisterPetActivity::class.java)
            startActivity(intent)
        }

    }
}

