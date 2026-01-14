package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R

class ServiceHistoryAdapter : RecyclerView.Adapter<ServiceHistoryAdapter.ServiceHistoryViewHolder>() {

    // Placeholder data
    private val serviceHistoryItems = listOf(
        ServiceHistory("27 Nov, 2025", "Completed", "Free", "CSR-1079032", "Schedule Service", "2988", "SUZUKI 5S ARENA"),
        ServiceHistory("15 Oct, 2024", "Completed", "Paid", "CSR-1065432", "Breakdown Service", "2500", "SUZUKI 5S ARENA")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_history, parent, false)
        return ServiceHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceHistoryViewHolder, position: Int) {
        val item = serviceHistoryItems[position]
        holder.tvDate.text = "Date: ${item.date}"
        holder.tvStatus.text = item.status
        holder.tvFree.text = item.type
        holder.tvServiceId.text = "Service ID: ${item.serviceId}"
        holder.tvServiceType.text = "Service Type: ${item.serviceType}"
        holder.tvOdometer.text = "Odometer (KMs): ${item.odometer}"
        holder.tvServiceCenter.text = "Service Center: ${item.serviceCenter}"
    }

    override fun getItemCount(): Int {
        return serviceHistoryItems.size
    }

    class ServiceHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvFree: TextView = itemView.findViewById(R.id.tvFree)
        val tvServiceId: TextView = itemView.findViewById(R.id.tvServiceId)
        val tvServiceType: TextView = itemView.findViewById(R.id.tvServiceType)
        val tvOdometer: TextView = itemView.findViewById(R.id.tvOdometer)
        val tvServiceCenter: TextView = itemView.findViewById(R.id.tvServiceCenter)
    }

    data class ServiceHistory(
        val date: String,
        val status: String,
        val type: String,
        val serviceId: String,
        val serviceType: String,
        val odometer: String,
        val serviceCenter: String
    )
}