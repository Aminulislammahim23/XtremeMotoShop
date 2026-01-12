package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Bike

class MyBikeAdapter(private val bikeList: ArrayList<Bike>) :
    RecyclerView.Adapter<MyBikeAdapter.MyBikeViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBikeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_bike, parent, false)
        return MyBikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBikeViewHolder, position: Int) {
        val bike = bikeList[position]
        
        // বাইকের নাম এবং মডেল সেট করা
        holder.tvBikeName.text = "${bike.name} ${bike.model}"
        
        // ইঞ্জিন নম্বর সেট করা
        holder.tvEngineNo.text = "Engine No: ${bike.engNum}"
        
        // রেজিস্ট্রেশন বা ফ্রি সার্ভিস (এখানে বাইকের রেজিস্ট্রেশন দেখানো হচ্ছে)
        holder.tvFreeService.text = "Reg: ${bike.reg}"

        // সিলেকশন লজিক (রেডিও বাটন)
        holder.rbSelected.isChecked = position == selectedPosition

        holder.itemView.setOnClickListener {
            val lastPos = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(lastPos)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = bikeList.size

    class MyBikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBikeName: TextView = itemView.findViewById(R.id.tvBikeName)
        val tvEngineNo: TextView = itemView.findViewById(R.id.tvEngineNo)
        val tvFreeService: TextView = itemView.findViewById(R.id.tvFreeService)
        val rbSelected: RadioButton = itemView.findViewById(R.id.rbSelected)
    }
}
