package com.app.medifindfinal

import android.content.Intent // Import Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem // Import MenuItem for back button
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity // Change to AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Remove Fragment from the class declaration
class SearchDoctorActivity : AppCompatActivity() {

    private lateinit var searchByNameEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var specializationSpinner: Spinner
    private lateinit var searchByLocationEditText: EditText
    private lateinit var availabilityEditText: EditText
    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val doctorsCollection = firestore.collection("doctors")

    // Move the code from onCreateView and onViewCreated to onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_doctor) // Set the layout for the Activity

        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        supportActionBar?.title = "Search Doctors" // Set toolbar title


        searchByNameEditText = findViewById(R.id.searchByNameEditText)
        searchButton = findViewById(R.id.searchButton)
        specializationSpinner = findViewById(R.id.specializationSpinner)
        searchByLocationEditText = findViewById(R.id.searchByLocationEditText)
        availabilityEditText = findViewById(R.id.availabilityEditText)
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)

        doctorsRecyclerView.layoutManager = LinearLayoutManager(this) // Use 'this' for context

        val specializationList = listOf("All", "Cardiologist", "Children", "Dentist", "General Physician", "ENT" , "Animals Doctor"/* Add more */)
        val specializationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, specializationList) // Use 'this' for context
        specializationSpinner.adapter = specializationAdapter

        doctorAdapter = DoctorAdapter(emptyList()) { doctor ->
            // Handle doctor item click - Launch DoctorProfileActivity
            val intent = Intent(this, DoctorProfileActivity::class.java).apply {
                putExtra("doctorId", doctor.id) // Pass the doctor ID as an extra
            }
            startActivity(intent)
        }
        doctorsRecyclerView.adapter = doctorAdapter
        // Fetch and display all doctors on activity creation
        performSearch()
        searchButton.setOnClickListener {
            performSearch()
        }
    }
    // Handle the back button in the toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    // Handle the system back button press
    // Since DoctorProfileFragment is now an Activity, we no longer need to pop fragments here
    override fun onBackPressed() {
        super.onBackPressed() // Simply go back to the previous activity (DashboardActivity)
    }
    private fun performSearch() {
        val nameQuery = searchByNameEditText.text.toString().trim()
        val specializationQuery = specializationSpinner.selectedItem.toString()
        val locationQuery = searchByLocationEditText.text.toString().trim()
        val availabilityQuery = availabilityEditText.text.toString().trim()

        var query: Query = doctorsCollection

        // Add filters based on user input
        if (!TextUtils.isEmpty(nameQuery)) {
            // Note: Firestore range filters on different fields require composite indexes.
            // For simple text search prefix matching is possible.
            // For robust text search consider using a dedicated search service (e.g., Algolia, Elasticsearch)
            query = query.whereGreaterThanOrEqualTo("name", nameQuery)
                .whereLessThanOrEqualTo("name", nameQuery + '\uf8ff')
        }
        if (specializationQuery != "All") {
            query = query.whereEqualTo("specialization", specializationQuery)
        }
        if (!TextUtils.isEmpty(locationQuery)) {
            // Similar to name search, range filter on location requires composite index.
            query = query.whereGreaterThanOrEqualTo("address", locationQuery)
                .whereLessThanOrEqualTo("address", locationQuery + '\uf8ff')
        }

        // Note: Filtering by availability string requires either a complex query or
        // structuring your data differently (e.g., storing availability as a map or array)
        // For simple string matching:
        if (!TextUtils.isEmpty(availabilityQuery)) {
            // This will only match exact availability strings
            query = query.whereEqualTo("availability", availabilityQuery)
            // For partial matching or more complex availability checks, you might need
            // to fetch all doctors and filter client-side, or use a different database structure/search solution.
        }


        query.get()
            .addOnSuccessListener { querySnapshot ->
                val doctorsList = mutableListOf<Doctor>()
                for (document in querySnapshot) {
                    // Ensure Doctor data class has a no-argument constructor
                    val doctor = document.toObject(Doctor::class.java).apply {
                        id = document.id
                    }
                    doctorsList.add(doctor)
                }
                doctorAdapter.updateData(doctorsList)
            }
            .addOnFailureListener { e ->
                // Use 'this' or applicationContext for Toast in Activity
                Toast.makeText(this, "Error fetching doctors: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SearchDoctorActivity", "Error fetching doctors", e) // Change Log tag
            }
    }

    // Keep the DoctorAdapter class as is, it's an inner class and uses itemView.context
    private class DoctorAdapter(
        private var doctors: List<Doctor>,
        private val onItemClick: (Doctor) -> Unit
    ) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {
        class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.doctorNameTextView)
            val specializationTextView: TextView = itemView.findViewById(R.id.doctorSpecializationTextView)
            val hospitalTextView: TextView = itemView.findViewById(R.id.doctorHospitalTextView)
            val imageView: ImageView = itemView.findViewById(R.id.doctorImageView)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doctor, parent, false)
            return DoctorViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
            val currentDoctor = doctors[position]
            holder.nameTextView.text = currentDoctor.name
            holder.specializationTextView.text = currentDoctor.specialization
            holder.hospitalTextView.text = currentDoctor.hospitalAffiliation

            // Use parent.context or holder.itemView.context with Glide in Adapter
            currentDoctor.photoUrl?.let { url ->
                Glide.with(holder.itemView.context)
                    .load(url)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(holder.imageView)
            } ?: run {
                holder.imageView.setImageResource(R.drawable.baseline_account_circle_24)
            }

            holder.itemView.setOnClickListener {
                currentDoctor.let {
                    onItemClick(it)
                }
            }
        }
        override fun getItemCount() = doctors.size
        fun updateData(newDoctors: List<Doctor>) {
            doctors = newDoctors
            notifyDataSetChanged()
        }
    }
}