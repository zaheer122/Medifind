// TimeSlotAdapter.kt
package com.app.medifindfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeSlotAdapter(
    private var timeSlots: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeSlotTextView: TextView = itemView.findViewById(R.id.timeSlotTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val currentTimeSlot = timeSlots[position]
        holder.timeSlotTextView.text = currentTimeSlot
        holder.itemView.setOnClickListener {
            onItemClick(currentTimeSlot)
        }
    }

    override fun getItemCount() = timeSlots.size

    fun updateData(newTimeSlots: List<String>) {
        timeSlots = newTimeSlots
        notifyDataSetChanged()
    }
}