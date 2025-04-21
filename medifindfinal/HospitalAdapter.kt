package com.app.medifindfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.DecimalFormat


class HospitalAdapter(private var hospitals: List<Hospital>) :
    RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.hospitalNameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.hospitalAddressTextView)
        val hospitalImageView: ImageView = itemView.findViewById(R.id.hospitalImageView)
        val distanceTextView: TextView = itemView.findViewById(R.id.hospitalDistanceTextView) // **Distance TextView**
        val availabilityTextView: TextView = itemView.findViewById(R.id.hospitalAvailabilityTextView) // New TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val currentHospital = hospitals[position]
        holder.nameTextView.text = currentHospital.name
        holder.addressTextView.text = currentHospital.address ?: "Address not available"

        // **Display Distance**
        if (currentHospital.distance != null) {
            val decimalFormat = DecimalFormat("#.##") // Format to 2 decimal places
            val distanceKm = decimalFormat.format(currentHospital.distance)
            holder.distanceTextView.text = "Distance: $distanceKm km" // Display with "km" unit
            holder.distanceTextView.visibility = View.VISIBLE // Make sure distance TextView is visible
        } else {
            holder.distanceTextView.visibility = View.GONE // Hide distance if not calculated (shouldn't happen)
        }

        // Display Availability
        if (currentHospital.openNow != null) {
            holder.availabilityTextView.visibility = View.VISIBLE
            if (currentHospital.openNow == true) {
                holder.availabilityTextView.text = "Open Now"
                holder.availabilityTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)) // Optional: Set color
            } else {
                holder.availabilityTextView.text = "Closed"
                holder.availabilityTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)) // Optional: Set color
            }
        } else {
            holder.availabilityTextView.visibility = View.GONE // Hide if no opening hours info
        }
        // Image Display
        if (!currentHospital.imageUrl.isNullOrBlank()) {
            holder.hospitalImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(currentHospital.imageUrl)
                .placeholder(R.drawable.round_airline_seat_individual_suite_24)
                .error(R.drawable.round_airline_seat_individual_suite_24)
                .into(holder.hospitalImageView)
        } else {
            holder.hospitalImageView.visibility = View.GONE
        }
    }

    override fun getItemCount() = hospitals.size

    fun updateData(newHospitals: List<Hospital>) {
        hospitals = newHospitals
        notifyDataSetChanged()
    }

    // Add this function to get the currently displayed hospitals (for map updates after filtering)
    fun getCurrentHospitals(): List<Hospital> {
        return hospitals
    }

    fun filterHospitals(query: String) {
        val filteredList = if (query.isBlank()) {
            hospitals // If query is empty, show the original list
        } else {
            hospitals.filter { hospital ->
                hospital.name.contains(query, ignoreCase = true) ||
                        hospital.address?.contains(query, ignoreCase = true) ?: false
            }
        }
        hospitals = filteredList
        notifyDataSetChanged()
    }
}