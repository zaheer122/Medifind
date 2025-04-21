package com.app.medifindfinal;
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale
class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView // Toolbar profile picture
    private lateinit var drawerProfileImageView: ImageView // Drawer header profile picture
    private lateinit var currentLocationTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val PICK_IMAGE_REQUEST = 123 // Request code for gallery intent
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Initialize UI elements
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        profileImageView = findViewById(R.id.imageViewProfile) // Toolbar ImageView
        currentLocationTextView = findViewById(R.id.textViewCurrentLocation)
        // Get reference to the profile ImageView in the drawer header
        val headerView = navigationView.getHeaderView(0)
        drawerProfileImageView = headerView.findViewById(R.id.imageView)
        val nearestHospitalsButton: Button = findViewById(R.id.buttonNearestHospitals)
        val pharmacyButton: Button = findViewById(R.id.buttonPharmacy)
        val bookAppointmentButton: Button = findViewById(R.id.buttonBookAppointment)
        val searchDoctorsButton: Button = findViewById(R.id.buttonSearchDoctors)
        val emergencyContactButton: Button = findViewById(R.id.buttonEmergencyContact)
        val logoutButton: Button = findViewById(R.id.buttonLogout)
        // Set up ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        // Set onClickListeners for each button
        nearestHospitalsButton.setOnClickListener {
            val intent = Intent(this, HospitalsActivity::class.java)
            startActivity(intent)
        }
        pharmacyButton.setOnClickListener {
            val intent = Intent(this, PharmaciesActivity::class.java)
            startActivity(intent)
        }
        bookAppointmentButton.setOnClickListener {
            // Check if user is logged in
            if (auth.currentUser != null) {
                // User is logged in, navigate to Appointment Activity
                navigateToAppointmentActivity()
            } else {
                // User is not logged in, navigate to Login Activity
                navigateToLoginActivity()
            }
        }
        searchDoctorsButton.setOnClickListener {
            Log.d("DashboardActivity", "Search Doctors button clicked")
            val intent = Intent(this, SearchDoctorActivity::class.java)
            startActivity(intent)
        }
        emergencyContactButton.setOnClickListener {
            val intent = Intent(this, EmergencyContactActivity::class.java)
            startActivity(intent)
        }
        logoutButton.setOnClickListener {
            logoutUser() // Call logout function when button is clicked
        }
        // Load Profile Picture for both toolbar and drawer
        loadUserProfileImage()
        // Set OnClickListener for the profile image in the toolbar to open the gallery
        profileImageView.setOnClickListener {
            openGallery()
        }
        // You might also want to make the drawer profile image clickable
        drawerProfileImageView.setOnClickListener {
            openGallery()
        }
        // Get and Set Current Location
        fetchCurrentLocation()
    }
    private fun openGallery() {
        // Check for READ_EXTERNAL_STORAGE permission before opening gallery
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE_REQUEST)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                // Load and set the image for both the toolbar and drawer ImageViews using Glide
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(profileImageView) // Update toolbar image
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(drawerProfileImageView) // Update drawer image
                saveProfilePicture(imageUri) // Save the Uri
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    private fun saveProfilePicture(imageUri: Uri) {
        // Save the Uri of the selected image to SharedPreferences
        val sharedPref = getSharedPreferences("profile_data", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("profile_image_uri", imageUri.toString())
            apply()
        }
        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
    }
    private fun loadUserProfileImage() {
        // Check if a manually selected image Uri is saved
        val sharedPref = getSharedPreferences("profile_data", MODE_PRIVATE)
        val savedImageUriString = sharedPref.getString("profile_image_uri", null)
        if (!savedImageUriString.isNullOrEmpty()) {
            val savedImageUri = Uri.parse(savedImageUriString)
            // Load the manually selected image using Glide for both ImageViews
            Glide.with(this)
                .load(savedImageUri)
                .placeholder(R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(profileImageView) // Load into toolbar image

            Glide.with(this)
                .load(savedImageUri)
                .placeholder(R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(drawerProfileImageView) // Load into drawer image

        } else {
            // Load from Firebase or default if no manually selected image
            val currentUser: FirebaseUser? = auth.currentUser
            if (currentUser != null) {
                val photoUrl: Uri? = currentUser.photoUrl
                if (photoUrl != null) {
                    // User is logged in with Google and has a profile picture URL
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .circleCrop()
                        .into(profileImageView) // Load into toolbar image

                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .circleCrop()
                        .into(drawerProfileImageView) // Load into drawer image

                } else {
                    // User is logged in but no Google profile picture (e.g., email/password signup)
                    profileImageView.setImageResource(R.drawable.baseline_account_circle_24) // Set default toolbar
                    drawerProfileImageView.setImageResource(R.drawable.baseline_account_circle_24) // Set default drawer
                }
            } else {
                // Guest user or not logged in
                profileImageView.setImageResource(R.drawable.baseline_account_circle_24) // Set default toolbar
                drawerProfileImageView.setImageResource(R.drawable.baseline_account_circle_24) // Set default drawer
            }
        }
    }
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle home click
                Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_hospitals -> {
                // Handle hospitals click
                startActivity(Intent(this, HospitalsActivity::class.java))
            }
            R.id.nav_book_appointment -> {
                // Handle change location click
                startActivity(Intent(this, ChooseDoctorActivity::class.java))
            }
            R.id.nav_change_theme -> {
                // Handle change theme click
                showThemeSelectionDialog()
            }
            R.id.nav_contact_us -> {
                // Handle contact us click
                startActivity(Intent(this, ContactUsActivity::class.java))
            }
            R.id.nav_feedback -> {
                // Handle feedback click
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return // Return early if permission is not granted yet, location will be fetched later if permission granted
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Got last known location. In some rare situations this can be null.
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses!!.isNotEmpty()) {
                            val address = addresses[0]
                            val city = address.locality ?: "Unknown City"
                            val state = address.adminArea ?: "Unknown State"
                            val country = address.countryName ?: "Unknown Country"
                            val currentLocation = "$city, $state, $country"
                            currentLocationTextView.text = currentLocation
                        } else {
                            currentLocationTextView.text = "Location not found"
                        }
                    } catch (e: Exception) {
                        currentLocationTextView.text = "Error fetching location"
                        e.printStackTrace()
                    }
                } else {
                    currentLocationTextView.text = "Location unavailable"
                }
            }
            .addOnFailureListener { e ->
                currentLocationTextView.text = "Location fetch failed"
                e.printStackTrace()
            }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, fetch location again
                fetchCurrentLocation()
            } else {
                // Permission denied, you could show a message or use default location
                currentLocationTextView.text = "Location permission denied"
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            // Handle permission result for gallery access
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openGallery() // If permission granted after request, open gallery
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
    private fun navigateToAppointmentActivity() {
        // Navigate to Appointment Activity
        startActivity(Intent(this, ChooseDoctorActivity::class.java))
    }
    private fun logoutUser() {
        auth.signOut() // Firebase Sign Out
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        navigateToLoginActivity() // Go back to Login Activity after logout
        finish() // Optional: Close DashboardActivity after logout
    }
    private fun showThemeSelectionDialog() {
        val themes = arrayOf("System Default", "Light", "Dark")
        val checkedItem = getSavedThemePreference() // Get the currently saved preference
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                // Save the selected theme preference
                saveThemePreference(which)
                // Apply the selected theme immediately
                applyTheme(which)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun getSavedThemePreference(): Int {
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        // Default to System Default if no preference is saved
        return sharedPref.getInt("theme_setting", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    private fun saveThemePreference(themeMode: Int) {
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("theme_setting", themeMode)
            apply()
        }
    }
    private fun applyTheme(themeMode: Int) {
        // Map dialog selection to AppCompatDelegate night mode constants
        val nightMode = when (themeMode) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // System Default
            1 -> AppCompatDelegate.MODE_NIGHT_NO // Light Theme
            2 -> AppCompatDelegate.MODE_NIGHT_YES // Dark Theme
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // Fallback to System Default
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}