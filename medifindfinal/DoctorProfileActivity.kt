package com.app.medifindfinal

import android.os.Bundle
import android.util.Log
import android.view.MenuItem // Import MenuItem for back button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity // Change to AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

// Remove Fragment from the class declaration
class DoctorProfileActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val doctorsCollection = firestore.collection("doctors")
    private var doctorId: String? = null

    private lateinit var doctorProfileImageView: ImageView
    private lateinit var doctorProfileNameTextView: TextView
    private lateinit var doctorProfileSpecializationTextView: TextView
    private lateinit var doctorProfileQualificationsTextView: TextView
    private lateinit var doctorProfileExperienceTextView: TextView
    private lateinit var doctorProfileHospitalTextView: TextView
    private lateinit var doctorProfileAvailabilityTextView: TextView
    private lateinit var doctorProfileContactTextView: TextView

    // Move the code from onCreateView and onViewCreated to onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile) // Set the layout for the Activity

        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        supportActionBar?.title = "Doctor Profile" // Set toolbar title


        doctorProfileImageView = findViewById(R.id.doctorProfileImageView)
        doctorProfileNameTextView = findViewById(R.id.doctorProfileNameTextView)
        doctorProfileSpecializationTextView = findViewById(R.id.doctorProfileSpecializationTextView)
        doctorProfileQualificationsTextView = findViewById(R.id.doctorProfileQualificationsTextView)
        doctorProfileExperienceTextView = findViewById(R.id.doctorProfileExperienceTextView)
        doctorProfileHospitalTextView = findViewById(R.id.doctorProfileHospitalTextView)
        doctorProfileAvailabilityTextView = findViewById(R.id.doctorProfileAvailabilityTextView)
        doctorProfileContactTextView = findViewById(R.id.doctorProfileContactTextView)

        // Get doctorId from the Intent extras instead of arguments
        doctorId = intent.getStringExtra("doctorId")


        if (!doctorId.isNullOrEmpty()) {
            fetchDoctorDetails(doctorId!!)
        } else {
            // Use 'this' or applicationContext for Toast in Activity
            Toast.makeText(this, "Doctor ID is missing", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if doctor ID is missing
        }
    }

    private fun fetchDoctorDetails(doctorId: String) {
        doctorsCollection.document(doctorId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    doctor?.let {
                        doctorProfileNameTextView.text = it.name
                        doctorProfileSpecializationTextView.text = it.specialization
                        doctorProfileQualificationsTextView.text = it.qualifications
                        // Handle potential null for experience and convert to string
                        doctorProfileExperienceTextView.text = it.experience?.toString() ?: ""
                        doctorProfileHospitalTextView.text = it.hospitalAffiliation
                        doctorProfileAvailabilityTextView.text = it.availability
                        // Ensure contactNumber and address are not null before concatenation
                        doctorProfileContactTextView.text = "${it.contactNumber ?: ""}\n${it.address ?: ""}"


                        it.photoUrl?.let { url ->
                            // Use 'this' or applicationContext with Glide in Activity
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.baseline_account_circle_24)
                                .into(doctorProfileImageView)
                        } ?: run {
                            doctorProfileImageView.setImageResource(R.drawable.baseline_account_circle_24)
                        }
                    }
                } else {
                    // Use 'this' or applicationContext for Toast in Activity
                    Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity if doctor not found
                }
            }
            .addOnFailureListener { e ->
                // Use 'this' or applicationContext for Toast in Activity
                Toast.makeText(this, "Error fetching doctor details: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DoctorProfileActivity", "Error fetching details", e) // Change Log tag
                finish() // Close the activity on failure
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
}