package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WaterTimesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButtonWaterTimes)
        returnButton.setOnClickListener {

            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }



    }
}