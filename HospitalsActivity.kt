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
class HospitalsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var searchEditText: EditText
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var hospitalsRecyclerView: RecyclerView
    private lateinit var hospitalAdapter: HospitalAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null
    private var currentLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private val GOOGLE_PLACES_API_KEY = "YOUR_GOOGLE_PLACES_API_KEY"
    private var allHospitalsList: List<Hospital> = emptyList()
    private var currentHospitalsList: List<Hospital> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospitals)
        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText)
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
        hospitalsRecyclerView = findViewById(R.id.hospitalsRecyclerView)
        // Set up RecyclerView
        hospitalsRecyclerView.layoutManager = LinearLayoutManager(this)
        hospitalAdapter = HospitalAdapter(emptyList())
        hospitalsRecyclerView.adapter = hospitalAdapter
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // **Set up TextWatcher for searchEditText**
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHospitals(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fetchCurrentLocationAndHospitals()
    }
    private fun fetchCurrentLocationAndHospitals() {
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
                    findNearbyPlaces("hospital")
                } else {
                    Toast.makeText(this, "Current location is unavailable.", Toast.LENGTH_SHORT).show()
                    val defaultLocation = LatLng(0.0, 0.0)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2f))
                    findNearbyPlaces("hospital")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LocationError", "Location fetch failed", e)
                val defaultLocation = LatLng(0.0, 0.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2f))
                findNearbyPlaces("hospital")
            }
    }
    private fun findNearbyPlaces(type: String) {
        if (userLocation == null) {
            Log.w("findNearbyPlaces", "User location is null, cannot perform nearby search")
            Toast.makeText(this, "User location is not yet available.", Toast.LENGTH_SHORT).show()
            return
        }
        if (GOOGLE_PLACES_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
            Toast.makeText(this, "Please set your GOOGLE_PLACES_API_KEY in HospitalsActivity.kt", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this@HospitalsActivity, "Error fetching places: ${e.message}", Toast.LENGTH_LONG).show()
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
        val hospitalsList = mutableListOf<Hospital>()
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
                val hospitalLocation = Location("hospitalLocation")
                hospitalLocation.latitude = latLng.latitude
                hospitalLocation.longitude = latLng.longitude

                var distanceInKm: Double? = null
                if (currentLocation != null) {
                    distanceInKm = currentLocation!!.distanceTo(hospitalLocation).toDouble().div(1000)
                }

                val openingHours = place.optJSONObject("opening_hours")
                val openNow = openingHours?.optBoolean("open_now")

                val hospital = Hospital(
                    name = name,
                    address = address,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    phoneNumber = phoneNumber,
                    imageUrl = imageUrl,
                    distance = distanceInKm,
                    openNow = openNow // Set the openNow status
                )
                hospitalsList.add(hospital)
            }
            hospitalsList.sortBy { hospital -> hospital.distance ?: Double.MAX_VALUE }
            allHospitalsList = hospitalsList
            currentHospitalsList = hospitalsList
            updateHospitalDataAndMap()
            hospitalAdapter.updateData(currentHospitalsList)
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@HospitalsActivity, "Error parsing places: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getPhotoUrl(photoReference: String): String {
        val maxWidth = 400
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$GOOGLE_PLACES_API_KEY"
    }
    private fun displayHospitalsOnMap(hospitals: List<Hospital>) {
        mMap.clear()
        hospitals.forEach { hospital ->
            val hospitalLocation = LatLng(hospital.latitude, hospital.longitude)
            mMap.addMarker(
                MarkerOptions()
                    .position(hospitalLocation)
                    .title(hospital.name)
                    .snippet(hospital.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }
    private fun filterHospitals(searchText: String) {
        val filteredHospitals = if (searchText.isBlank()) {
            allHospitalsList
        } else {
            allHospitalsList.filter { hospital ->
                hospital.name.contains(searchText, ignoreCase = true) ||
                        hospital.address?.contains(searchText, ignoreCase = true) == true
            }
        }.toMutableList() // Convert to MutableList for sorting
        // **SORT FILTERED HOSPITALS BY DISTANCE HERE in filterHospitals**
        filteredHospitals.sortBy { hospital -> hospital.distance ?: Double.MAX_VALUE } // Sort by distance, nearest first
        currentHospitalsList = filteredHospitals
        updateHospitalDataAndMap()
        hospitalAdapter.updateData(currentHospitalsList)
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
    private fun updateHospitalDataAndMap() {
        hospitalAdapter.updateData(currentHospitalsList)
        displayHospitalsOnMap(currentHospitalsList)
    }
}