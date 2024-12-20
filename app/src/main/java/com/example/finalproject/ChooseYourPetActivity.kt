package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChooseYourPetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_pet)

        val buttonAddNew = findViewById<ImageButton>(R.id.AddNewPetButton)
        buttonAddNew.setOnClickListener {
            val intent = Intent(this, RegisterPetActivity::class.java)
            startActivity(intent)
        }
    }
}

