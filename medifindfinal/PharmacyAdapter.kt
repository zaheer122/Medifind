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

class PharmacyAdapter(private var pharmacyList: List<Pharmacy>) :
    RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder>() {

    class PharmacyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.pharmacyNameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.pharmacyAddressTextView)
        val pharmacyImageView: ImageView = itemView.findViewById(R.id.pharmacyImageView)
        val distanceTextView: TextView = itemView.findViewById(R.id.pharmacyDistanceTextView)
        val availabilityTextView: TextView = itemView.findViewById(R.id.pharmacyAvailabilityTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmacyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pharmacy, parent, false)
        return PharmacyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PharmacyViewHolder, position: Int) {
        val currentPharmacy = pharmacyList[position]
        holder.nameTextView.text = currentPharmacy.name
        holder.addressTextView.text = currentPharmacy.address

        // **Display Distance**
        if (currentPharmacy.distance != null) {
            val decimalFormat = DecimalFormat("#.##") // Format to 2 decimal places
            val distanceKm = decimalFormat.format(currentPharmacy.distance)
            holder.distanceTextView.text = "Distance: $distanceKm km" // Display with "km" unit
            holder.distanceTextView.visibility = View.VISIBLE // Make sure distance TextView is visible
        } else {
            holder.distanceTextView.visibility = View.GONE // Hide distance if not calculated (shouldn't happen)
        }


        // Display Availability
        if (currentPharmacy.openNow != null) {
            holder.availabilityTextView.visibility = View.VISIBLE
            if (currentPharmacy.openNow == true) {
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
        if (!currentPharmacy.photoUrl.isNullOrBlank()) {
            holder.pharmacyImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(currentPharmacy.photoUrl)
                .placeholder(R.drawable.baseline_close_24)
                .error(R.drawable.baseline_close_24)
                .into(holder.pharmacyImageView)
        } else {
            holder.pharmacyImageView.visibility = View.GONE
        }
    }

    override fun getItemCount() = pharmacyList.size

    fun updateData(newPharmacyList: List<Pharmacy>) {
        pharmacyList = newPharmacyList
        notifyDataSetChanged()
    }
}