package com.app.medifindfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


class PharmaciesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var searchEditText: EditText
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var pharmaciesRecyclerView: RecyclerView
    private lateinit var pharmacyAdapter: PharmacyAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null
    private var currentLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 2001 // Changed request code
    private val GOOGLE_PLACES_API_KEY = "AIzaSyDkG8M279L02NQlcmwLV6XbgX3L7IsV6Ow" // Ensure this is your correct API key

    private var allPharmaciesList: List<Pharmacy> = emptyList()
    private var currentPharmaciesList: List<Pharmacy> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pharmicies) // Changed layout file

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText)
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
        pharmaciesRecyclerView = findViewById(R.id.pharmaciesRecyclerView) // Changed RecyclerView ID

        // Set up RecyclerView
        pharmaciesRecyclerView.layoutManager = LinearLayoutManager(this)
        pharmacyAdapter = PharmacyAdapter(emptyList())
        pharmaciesRecyclerView.adapter = pharmacyAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // **Set up TextWatcher for searchEditText**
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPharmacies(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fetchCurrentLocationAndPharmacies()
    }


    private fun fetchCurrentLocationAndPharmacies() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    userLocation = LatLng(location.latitude, location.longitude)
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 10f))
                    findNearbyPlaces("pharmacy") // Changed type to "pharmacy"
                } else {
                    Toast.makeText(this, "Current location is unavailable.", Toast.LENGTH_SHORT).show()
                    val defaultLocation = LatLng(0.0, 0.0)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2f))
                    findNearbyPlaces("pharmacy") // Changed type to "pharmacy"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LocationError", "Location fetch failed", e)
                val defaultLocation = LatLng(0.0, 0.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2f))
                findNearbyPlaces("pharmacy") // Changed type to "pharmacy"
            }
    }


    private fun findNearbyPlaces(type: String) {
        if (userLocation == null) {
            Log.w("findNearbyPlaces", "User location is null, cannot perform nearby search")
            Toast.makeText(this, "User location is not yet available.", Toast.LENGTH_SHORT).show()
            return
        }

        if (GOOGLE_PLACES_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
            Toast.makeText(this, "Please set your GOOGLE_PLACES_API_KEY in PharmaciesActivity.kt", Toast.LENGTH_LONG).show()
            Log.e("Google Places API", "GOOGLE_PLACES_API_KEY is not set! Replace 'YOUR_GOOGLE_PLACES_API_KEY' with your actual API key.")
            return
        }


        val apiKey = GOOGLE_PLACES_API_KEY
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${userLocation!!.latitude},${userLocation!!.longitude}&radius=40000&type=$type&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PharmaciesActivity, "Error fetching places: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    parsePlaces(responseData)
                }
            }
        })
    }

    private fun parsePlaces(response: String?) {
        val pharmaciesList = mutableListOf<Pharmacy>() // Changed to pharmaciesList
        try {
            val jsonObject = JSONObject(response!!)
            val results = jsonObject.getJSONArray("results")

            for (i in 0 until results.length()) {
                val place = results.getJSONObject(i)
                val geometry = place.getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                val latLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                val name = place.getString("name")
                val address = place.getString("vicinity")
                val phoneNumber = place.optString("formatted_phone_number", null)
                val imageUrl = if (place.has("photos")) {
                    val photo = place.getJSONArray("photos").getJSONObject(0)
                    val photoReference = photo.getString("photo_reference")
                    getPhotoUrl(photoReference)
                } else {
                    null
                }

                val pharmacyLocation = Location("pharmacyLocation") // Changed location name
                pharmacyLocation.latitude = latLng.latitude
                pharmacyLocation.longitude = latLng.longitude

                var distanceInKm: Double? = null
                if (currentLocation != null) {
                    distanceInKm = currentLocation!!.distanceTo(pharmacyLocation).toDouble().div(1000)
                }

                val openingHours = place.optJSONObject("opening_hours")
                val openNow = openingHours?.optBoolean("open_now")


                val pharmacy = Pharmacy( // Using Pharmacy data class
                    name = name,
                    address = address,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    phone = phoneNumber ?: "", // Assuming phone field in Pharmacy
                    photoUrl = imageUrl,
                    distance = distanceInKm,
                    openNow = openNow
                )
                pharmaciesList.add(pharmacy)
            }

            // **SORT PHARMACIES BY DISTANCE HERE in parsePlaces**
            pharmaciesList.sortBy { pharmacy -> pharmacy.distance ?: Double.MAX_VALUE } // Sort by distance, nearest first

            allPharmaciesList = pharmaciesList // Changed to allPharmaciesList
            currentPharmaciesList = pharmaciesList // Changed to currentPharmaciesList
            updatePharmacyDataAndMap() // Changed function name
            pharmacyAdapter.updateData(currentPharmaciesList) // Using pharmacyAdapter


        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@PharmaciesActivity, "Error parsing places: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPhotoUrl(photoReference: String): String {
        val maxWidth = 400
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$GOOGLE_PLACES_API_KEY"
    }


    private fun displayPharmaciesOnMap(pharmacies: List<Pharmacy>) { // Changed to displayPharmaciesOnMap
        mMap.clear()
        pharmacies.forEach { pharmacy ->
            val pharmacyLocation = LatLng(pharmacy.latitude!!, pharmacy.longitude!!) // Using non-null assertion as latitude/longitude should be available
            mMap.addMarker(
                MarkerOptions()
                    .position(pharmacyLocation)
                    .title(pharmacy.name)
                    .snippet(pharmacy.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Changed marker color
            )
        }
    }

    private fun filterPharmacies(searchText: String) { // Changed to filterPharmacies
        val filteredPharmacies = if (searchText.isBlank()) {
            allPharmaciesList
        } else {
            allPharmaciesList.filter { pharmacy ->
                pharmacy.name?.contains(searchText, ignoreCase = true) == true ||
                        pharmacy.address?.contains(searchText, ignoreCase = true) == true
            }
        }.toMutableList() // Convert to MutableList for sorting

        // **SORT FILTERED PHARMACIES BY DISTANCE HERE in filterPharmacies**
        filteredPharmacies.sortBy { pharmacy -> pharmacy.distance ?: Double.MAX_VALUE } // Sort by distance, nearest first


        currentPharmaciesList = filteredPharmacies // Changed to currentPharmaciesList
        updatePharmacyDataAndMap() // Changed function name
        pharmacyAdapter.updateData(currentPharmaciesList) // Using pharmacyAdapter
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePharmacyDataAndMap() { // Changed function name
        pharmacyAdapter.updateData(currentPharmaciesList)
        displayPharmaciesOnMap(currentPharmaciesList)
    }
}