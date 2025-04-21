package com.app.medifindfinal
data class Hospital(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val openNow: Boolean? = null,
    val phoneNumber: String?, // Phone number might be optional in API response
    val imageUrl: String? = null, // Image URL might be optional
    val distance: Double? = null       // Distance might be calculated later
)
