// DoctorAdapter.kt
package com.app.medifindfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DoctorAdapter(
    private var doctorsList: List<Doctor>,
    private val onItemClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.doctorNameTextView)
        val specializationTextView: TextView = itemView.findViewById(R.id.doctorSpecializationTextView)
        val clinicsTextView: TextView = itemView.findViewById(R.id.doctorClinicsTextView)
        val bookButton: Button = itemView.findViewById(R.id.bookAppointmentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_appointment, parent, false)
        return DoctorViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val currentDoctor = doctorsList[position]
        holder.nameTextView.text = currentDoctor.name
        holder.specializationTextView.text = currentDoctor.specialization
        holder.clinicsTextView.text = "${currentDoctor.clinicCount} clinics" // Assuming your Doctor class has clinicCount
        holder.bookButton.setOnClickListener {
            onItemClick(currentDoctor)
        }
    }

    override fun getItemCount() = doctorsList.size

    fun updateData(newDoctorsList: List<Doctor>) {
        doctorsList = newDoctorsList
        notifyDataSetChanged()
    }
}