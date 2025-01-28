package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

/**
 * MainPageActivity is the main activity where the user can access various features of the application,
 * such as viewing food and water times, symptoms, healthcare, health history, nearby vets, and emergency services.
 * It handles the navigation to respective activities through buttons.
 */
class MainPageActivity : AppCompatActivity() {

    /**
     * This method is called when the activity is created.
     * It sets up the UI elements and defines click listeners for buttons to navigate to other activities.
     *
     * @param savedInstanceState a Bundle object that contains the activity's previously saved state, or null if there is no state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        // Set up the "Food" button to navigate to the FoodTimesActivity when clicked
        val FoodButton = findViewById<ImageButton>(R.id.FoodButton)
        FoodButton.setOnClickListener {

            val intent = Intent(this, FoodTimesActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Water" button to navigate to the WaterTimesActivity when clicked
        val WaterButton = findViewById<ImageButton>(R.id.WaterButton)
        WaterButton.setOnClickListener {

            val intent = Intent(this, WaterTimesActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Symptoms" button to navigate to the SymptomsAddActivity when clicked
        val SymptomsButton = findViewById<ImageButton>(R.id.SymptomsButton)
        SymptomsButton.setOnClickListener {

            val intent = Intent(this, SymptomsAddActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Health History" button to navigate to the HealthInfoView when clicked
        val HealthHistoryButton = findViewById<ImageButton>(R.id.HealthHistoryButton)
        HealthHistoryButton.setOnClickListener {

            val intent = Intent(this, Historyforsymptomsandcare::class.java)
            startActivity(intent)
        }

        // Set up the "Vets Nearby" button to navigate to the VetsNearbyActivity when clicked
        val VetsNearbyButton = findViewById<ImageButton>(R.id.VetsNearbyButton)
        VetsNearbyButton.setOnClickListener {

            val intent = Intent(this, VetsNearbyActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Emergency" button to navigate to the EmergencyActivity when clicked
        val EmergencyButton = findViewById<ImageButton>(R.id.EmergencyyButton)
        EmergencyButton.setOnClickListener {

            val intent = Intent(this, EmergencyActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Go Back" button to navigate to the ChooseYourPetActivity when clicked
        val GoBackButton = findViewById<ImageButton>(R.id.GoBackButton)
        GoBackButton.setOnClickListener {

            val intent = Intent(this, ChooseYourPetActivity::class.java)
            startActivity(intent)
        }
    }
}