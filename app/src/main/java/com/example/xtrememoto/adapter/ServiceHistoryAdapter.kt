package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.ui.service.BookingModel

class ServiceHistoryAdapter(
    private var list: List<BookingModel>,
    private val onItemClick: (BookingModel) -> Unit
) : RecyclerView.Adapter<ServiceHistoryAdapter.ServiceHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_history, parent, false)
        return ServiceHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceHistoryViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = "Date: ${item.date}"
        holder.tvStatus.text = item.status
        holder.tvServiceType.text = "Service Type: ${item.category}"
        holder.tvServiceCenter.text = "Service Center: ${item.dealer}"
        
        // আইডি এবং ওডোমিটার ডাটাবেসে না থাকলে হাইড বা টাইম শো করা হয়েছে
        holder.tvServiceId.text = "Time: ${item.time}"
        holder.tvOdometer.visibility = View.GONE
        holder.tvFree.visibility = View.GONE

        // View Details ক্লিক হ্যান্ডেল
        holder.tvViewDetails.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    class ServiceHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvFree: TextView = itemView.findViewById(R.id.tvFree)
        val tvServiceId: TextView = itemView.findViewById(R.id.tvServiceId)
        val tvServiceType: TextView = itemView.findViewById(R.id.tvServiceType)
        val tvOdometer: TextView = itemView.findViewById(R.id.tvOdometer)
        val tvServiceCenter: TextView = itemView.findViewById(R.id.tvServiceCenter)
        val tvViewDetails: TextView = itemView.findViewById(R.id.tvViewDetails)
    }
}
