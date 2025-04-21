// AppointmentConfirmationActivity.kt
package com.app.medifindfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppointmentConfirmationActivity : AppCompatActivity() {

    private lateinit var confirmationDetailsTextView: TextView
    private lateinit var doneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_confirmation)

        confirmationDetailsTextView = findViewById(R.id.confirmationDetailsTextView)
        doneButton = findViewById(R.id.doneButton)

        // Retrieve appointment details from the previous activity if passed
        val doctorId = intent.getStringExtra("doctorId")
        val doctorName = intent.getStringExtra("doctorName") // Retrieve doctor's name
        val hospitalAffiliation = intent.getStringExtra("hospitalAffiliation") // Retrieve hospital affiliation
        val selectedDate = intent.getStringExtra("selectedDate")
        val selectedTime = intent.getStringExtra("selectedTime")
        // Fetch doctor name or other details if needed

        val confirmationText = StringBuilder()
        doctorName?.let { confirmationText.append("Doctor: $it\n") } // Add doctor's name
        doctorId?.let { confirmationText.append("Doctor ID: $it\n") }
        hospitalAffiliation?.let { confirmationText.append("Hospital: $it\n") } // Add hospital affiliation
        selectedDate?.let { confirmationText.append("Date: $it\n") }
        selectedTime?.let { confirmationText.append("Time: $it\n") }
        // Add clinic details if available

        if (confirmationText.isNotEmpty()) {
            confirmationDetailsTextView.text = confirmationText.toString()
        }

        doneButton.setOnClickListener {
            // Navigate back to the dashboard or any other relevant screen
            val intent = Intent(this, DashboardActivity::class.java) // Replace with your dashboard activity
            startActivity(intent)
            finishAffinity() // Close all previous activities
        }
    }
}