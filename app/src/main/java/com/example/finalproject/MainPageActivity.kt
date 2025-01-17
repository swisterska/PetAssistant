package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class MainPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)


        val FoodButton = findViewById<ImageButton>(R.id.FoodButton)
        FoodButton.setOnClickListener {

            val intent = Intent(this, FoodTimesActivity::class.java)
            startActivity(intent)
        }

        val WaterButton = findViewById<ImageButton>(R.id.WaterButton)
        WaterButton.setOnClickListener {

            val intent = Intent(this, WaterTimesActivity::class.java)
            startActivity(intent)
        }

        val SymptomsButton = findViewById<ImageButton>(R.id.SymptomsButton)
        SymptomsButton.setOnClickListener {

            val intent = Intent(this, SymptomsAddActivity::class.java)
            startActivity(intent)
        }

        val HealthCareButton = findViewById<ImageButton>(R.id.HealthCareButton)
        HealthCareButton.setOnClickListener {

            val intent = Intent(this, HealthCareView::class.java)
            startActivity(intent)
        }

        val HealthHistoryButton = findViewById<ImageButton>(R.id.HealthHistoryButton)
        HealthHistoryButton.setOnClickListener {

            val intent = Intent(this, HealthInfoView::class.java)
            startActivity(intent)
        }

        val VetsNearbyButton = findViewById<ImageButton>(R.id.VetsNearbyButton)
        VetsNearbyButton.setOnClickListener {

            val intent = Intent(this, VetsNearbyActivity::class.java)
            startActivity(intent)
        }

        val EmergencyButton = findViewById<ImageButton>(R.id.EmergencyyButton)
        EmergencyButton.setOnClickListener {

            val intent = Intent(this, EmergencyActivity::class.java)
            startActivity(intent)
        }

        val GoBackButton = findViewById<ImageButton>(R.id.GoBackButton)
        GoBackButton.setOnClickListener {

            val intent = Intent(this, ChooseYourPetActivity::class.java)
            startActivity(intent)
        }


}}