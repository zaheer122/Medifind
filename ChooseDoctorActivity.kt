package com.app.medifindfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.firestore.FirebaseFirestore

class ChooseDoctorActivity : AppCompatActivity() {

    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private val db = FirebaseFirestore.getInstance()
    private val doctorsCollection = db.collection("doctors")
    private val TAG = "ChooseDoctorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_doctor)
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)
        doctorsRecyclerView.layoutManager = LinearLayoutManager(this)
        doctorAdapter = DoctorAdapter(emptyList()) { doctor ->
            // Handle doctor item click to navigate to the next screen
            val intent = Intent(this, ChooseAppointmentSlotActivity::class.java)
            intent.putExtra("doctorId", doctor.id) // Assuming your Doctor data class has an ID
            intent.putExtra("doctorName", doctor.name)
            intent.putExtra("hospitalAffiliation", doctor.hospitalAffiliation) // Assuming your Doctor data class has this property
            startActivity(intent)
        }
        doctorsRecyclerView.adapter = doctorAdapter

        fetchDoctors()
    }

    private fun fetchDoctors() {
        doctorsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val doctorsList = mutableListOf<Doctor>()
                for (document in querySnapshot) {
                    val doctor = document.toObject(Doctor::class.java).apply {
                        id = document.id // Assuming you want to store the document ID
                    }
                    doctorsList.add(doctor)
                }
                doctorAdapter.updateData(doctorsList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching doctors", e)
                Toast.makeText(this, "Failed to fetch doctors", Toast.LENGTH_SHORT).show()
            }
    }
}