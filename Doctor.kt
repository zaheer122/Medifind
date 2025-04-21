package com.app.medifindfinal
data class Doctor(
    var id: String? = null, // Document ID from Firebase
    val name: String? = null,
    val specialization: String? = null,
    val qualifications: String? = null,
    val experience: Int? = null,
    val hospitalAffiliation: String? = null,
    val availability: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
    val clinicCount: Int = 0,
    val photoUrl: String? = null // URL to the doctor's photo in Firebase Storage (optional)
)

//Mon-Fri , 9:00 am to 5:00 am , Sunday, Weekends
//Rainbow Hospital , Children Care Hospital , Care Hospital , Royal Treatment Client
//Cardiologist", "Children", "Dentist", "General Physician", "ENT" , "Animals Doctor"
//MD in General Medicine , M.B.B.S , MD in Cardiology ,