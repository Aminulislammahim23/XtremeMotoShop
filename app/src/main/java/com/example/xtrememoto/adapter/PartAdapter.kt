package com.example.xtrememoto.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Part
import java.io.File

class PartAdapter(
    private var partList: List<Part>,
    private val onAddToCartClick: (Part) -> Unit
) : RecyclerView.Adapter<PartAdapter.PartViewHolder>() {

    class PartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPartImage: ImageView = view.findViewById(R.id.ivPartImage)
        val tvPartCategory: TextView = view.findViewById(R.id.tvPartCategory)
        val tvPartName: TextView = view.findViewById(R.id.tvPartName)
        val tvPartPrice: TextView = view.findViewById(R.id.tvPartPrice)
        val btnAddToCart: ImageButton = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part, parent, false)
        return PartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = partList[position]
        
        holder.tvPartCategory.text = part.categoryName?.uppercase() ?: "GENERAL"
        holder.tvPartName.text = if (!part.brand.isNullOrEmpty()) "${part.type} (${part.brand})" else part.type ?: "Unknown Part"
        holder.tvPartPrice.text = "৳ ${part.price ?: "0"}"
        
        if (!part.img.isNullOrEmpty()) {
            val file = File(part.img!!)
            if (file.exists()) {
                holder.ivPartImage.setImageURI(Uri.fromFile(file))
                holder.ivPartImage.alpha = 1.0f
            } else {
                holder.ivPartImage.setImageResource(R.drawable.ic_parts)
                holder.ivPartImage.alpha = 0.5f
            }
        } else {
            holder.ivPartImage.setImageResource(R.drawable.ic_parts)
            holder.ivPartImage.alpha = 0.5f
        }

        holder.btnAddToCart.setOnClickListener {
            onAddToCartClick(part)
        }
    }

    override fun getItemCount(): Int = partList.size

    fun updateList(newList: List<Part>) {
        partList = newList
        notifyDataSetChanged()
    }
}
