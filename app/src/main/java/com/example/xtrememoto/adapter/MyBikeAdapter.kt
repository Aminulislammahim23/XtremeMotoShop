package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R

class MyBikeAdapter : RecyclerView.Adapter<MyBikeAdapter.MyBikeViewHolder>() {

    // Placeholder data for now
    private val bikeList = listOf(
        "GIXXER NEW SERISE FI DISC V4"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBikeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_bike, parent, false)
        return MyBikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBikeViewHolder, position: Int) {
        holder.tvBikeName.text = bikeList[position]
    }

    override fun getItemCount(): Int = bikeList.size

    class MyBikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBikeName: TextView = itemView.findViewById(R.id.tvBikeName)
    }
}