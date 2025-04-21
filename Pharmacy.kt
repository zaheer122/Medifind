package com.app.medifindfinal

data class Pharmacy(
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val distance: Double? = null,
    val openNow: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
    // Add other relevant fields if needed
)