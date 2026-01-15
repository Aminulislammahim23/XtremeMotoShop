package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Part

class PartAdapter(private var partList: List<Part>) : RecyclerView.Adapter<PartAdapter.PartViewHolder>() {

    class PartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPartImage: ImageView = view.findViewById(R.id.ivPartImage)
        val tvPartCategory: TextView = view.findViewById(R.id.tvPartCategory)
        val tvPartName: TextView = view.findViewById(R.id.tvPartName)
        val tvPartPrice: TextView = view.findViewById(R.id.tvPartPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part, parent, false)
        return PartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = partList[position]
        
        // Use safe call (?.) and Elvis operator (?:) to handle null values
        holder.tvPartCategory.text = part.category?.uppercase() ?: "GENERAL"
        holder.tvPartName.text = part.type ?: "Unknown Part"
        holder.tvPartPrice.text = "৳ ${part.price ?: "0"}"
        
        // Placeholder image setting
        holder.ivPartImage.setImageResource(R.drawable.ic_parts)
    }

    override fun getItemCount(): Int = partList.size

    fun updateList(newList: List<Part>) {
        partList = newList
        notifyDataSetChanged()
    }
}
