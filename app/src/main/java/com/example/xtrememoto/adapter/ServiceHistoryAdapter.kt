package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R

class ServiceHistoryAdapter : RecyclerView.Adapter<ServiceHistoryAdapter.ServiceHistoryViewHolder>() {

    // This is a placeholder for your service history data. 
    // You should replace this with your actual data.
    private val serviceHistoryItems = listOf(
        "Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_history, parent, false)
        return ServiceHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceHistoryViewHolder, position: Int) {
        // This is where you would bind your data to the view. 
        // For now, we are just displaying the placeholder data.
    }

    override fun getItemCount(): Int {
        return serviceHistoryItems.size
    }

    class ServiceHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}