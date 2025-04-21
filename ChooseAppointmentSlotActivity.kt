// ChooseAppointmentSlotActivity.kt
package com.app.medifindfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ChooseAppointmentSlotActivity : AppCompatActivity() {

    private lateinit var doctorNameTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var timeSlotsRecyclerView: RecyclerView
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ChooseSlotActivity"
    private var selectedDate: String = ""
    private var doctorId: String? = null
    private var doctorName: String? = null // You might want to fetch this from Firebase
    private var hospitalAffiliation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_appointment_slot)

        doctorId = intent.getStringExtra("doctorId")
        doctorId = intent.getStringExtra("doctorId")
        doctorName = intent.getStringExtra("doctorName") // Retrieve the doctor's name from the intent
        hospitalAffiliation = intent.getStringExtra("hospitalAffiliation") // Retrieve hospital affiliation

        doctorNameTextView = findViewById(R.id.doctorNameTextView)
        doctorNameTextView.text = doctorName ?: "Doctor Name Not Found" // Set the doctor's name
        // Fetch doctor details (name, clinics, etc.) using doctorId if needed



        calendarView = findViewById(R.id.calendarView)
        timeSlotsRecyclerView = findViewById(R.id.timeSlotsRecyclerView)
        timeSlotsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Example time slots - replace with your logic to fetch available slots
        val availableTimeSlots = listOf("09:00 AM - 10:00 AM", "10:30 AM - 11:30 AM", "03:00 PM - 04:00 PM", "04:30 PM - 05:30 PM")
        timeSlotAdapter = TimeSlotAdapter(availableTimeSlots) { timeSlot ->
            // Handle time slot click - navigate to enter details screen
            val intent = Intent(this, EnterAppointmentDetailsActivity::class.java)
            intent.putExtra("doctorId", doctorId)
            intent.putExtra("doctorName", doctorName) // Pass the doctor's name
            intent.putExtra("hospitalAffiliation", hospitalAffiliation) // Assuming you have retrieved this from the previous intent
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedTime", timeSlot)
            // You might also need to handle clinic selection here if the doctor has multiple clinics
            startActivity(intent)
        }
        timeSlotsRecyclerView.adapter = timeSlotAdapter

        // Set up CalendarView listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)
            Log.d(TAG, "Selected date: $selectedDate")
            // Here you would typically fetch the available time slots for the selected doctor and date
            // and update the timeSlotAdapter
        }

        // Initialize with the current date
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(calendar.time)
        Log.d(TAG, "Initial selected date: $selectedDate")
        // Fetch initial time slots if needed
    }
}