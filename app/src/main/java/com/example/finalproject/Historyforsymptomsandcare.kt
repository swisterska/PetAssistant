package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Historyforsymptomsandcare : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_historyforsymptomsandcare)

        val returnButton = findViewById<ImageButton>(R.id.GoBackButtonHistory)
        returnButton.setOnClickListener {

            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        val symptomsHistory = findViewById<ImageButton>(R.id.SymptomsHistory)
        symptomsHistory.setOnClickListener {

            val intent = Intent(this, HealthInfoView::class.java)
            startActivity(intent)
        }
}}