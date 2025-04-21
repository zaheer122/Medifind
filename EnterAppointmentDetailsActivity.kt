// EnterAppointmentDetailsActivity.kt
package com.app.medifindfinal

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EnterAppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var contactNoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var confirmAppointmentButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")
    private val TAG = "EnterDetailsActivity"

    private var doctorId: String? = null
    private var doctorName: String? = null
    private var hospitalAffiliation: String? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var clinicId: String? = null // You might need to handle clinic ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_appointment_details)

        doctorId = intent.getStringExtra("doctorId")
        selectedDate = intent.getStringExtra("selectedDate")
        selectedTime = intent.getStringExtra("selectedTime")
        hospitalAffiliation = intent.getStringExtra("hospitalAffiliation")

        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        contactNoEditText = findViewById(R.id.contactNoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        confirmAppointmentButton = findViewById(R.id.confirmAppointmentButton)

        confirmAppointmentButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val contactNo = contactNoEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            val intent = Intent(this, AppointmentConfirmationActivity::class.java)
            intent.putExtra("doctorId", doctorId)
            intent.putExtra("doctorName", doctorName) // Assuming you have retrieved this from the previous intent
            intent.putExtra("hospitalAffiliation", hospitalAffiliation) // Assuming you have retrieved this from the previous intent
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedTime", selectedTime)
            startActivity(intent)

            if (validateInputs(firstName, lastName, contactNo, email)) {
                bookAppointment(firstName, lastName, contactNo, email)
            }
        }
    }

    private fun validateInputs(firstName: String, lastName: String, contactNo: String, email: String): Boolean {
        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.error = "First Name is required"
            return false
        }
        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.error = "Last Name is required"
            return false
        }
        if (TextUtils.isEmpty(contactNo)) {
            contactNoEditText.error = "Contact No is required"
            return false
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email"
            return false
        }
        return true
    }

    private fun bookAppointment(firstName: String, lastName: String, contactNo: String, email: String) {
        doctorId?.let { doctorId ->
            selectedDate?.let { date ->
                selectedTime?.let { time ->
                    val appointmentDateTime = "$date $time" // Combine date and time
                    val appointmentData = hashMapOf(
                        "doctorId" to doctorId,
                        "dateTime" to appointmentDateTime,
                        "clinicId" to clinicId, // Add clinic ID if applicable
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "contactNo" to contactNo,
                        "email" to email,
                        "status" to "pending"
                    )

                    appointmentsCollection.add(appointmentData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "Appointment booked with ID: ${documentReference.id}")
                            Toast.makeText(this, "Appointment request submitted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AppointmentConfirmationActivity::class.java)
                            // You can pass appointment details to the confirmation activity if needed
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error booking appointment", e)
                            Toast.makeText(this, "Failed to book appointment", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } ?: run {
            Toast.makeText(this, "Error: Doctor or time slot not selected", Toast.LENGTH_SHORT).show()
        }
    }
}