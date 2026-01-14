package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.ShopBike

class ShopBikeAdapter(
    private val onItemClick: (ShopBike) -> Unit
) : ListAdapter<ShopBike, ShopBikeAdapter.BikeViewHolder>(DiffCallback) {

    inner class BikeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // item_bike.xml এর আইডির সাথে মিল রাখা হয়েছে
        val tvBrand: TextView = view.findViewById(R.id.bike_make)
        val tvName: TextView = view.findViewById(R.id.bike_model)
        val tvPrice: TextView = view.findViewById(R.id.bike_price)
        val imgBike: ImageView = view.findViewById(R.id.bike_image)

        fun bind(bike: ShopBike) {
            tvBrand.text = bike.brand
            tvName.text = bike.name
            tvPrice.text = "৳ ${bike.price}"
            imgBike.setImageResource(R.drawable.ic_bike_placeholder)
            
            itemView.setOnClickListener { onItemClick(bike) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bike, parent, false)
        return BikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BikeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ShopBike>() {
        override fun areItemsTheSame(oldItem: ShopBike, newItem: ShopBike): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShopBike, newItem: ShopBike): Boolean {
            return oldItem == newItem
        }
    }
}