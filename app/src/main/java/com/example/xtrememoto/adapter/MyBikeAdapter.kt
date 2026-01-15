package com.example.xtrememoto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Bike
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyBikeAdapter(private val bikeList: ArrayList<Bike>) :
    RecyclerView.Adapter<MyBikeAdapter.MyBikeViewHolder>() {

    private var selectedPosition = -1
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBikeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_bike, parent, false)
        return MyBikeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBikeViewHolder, position: Int) {
        val bike = bikeList[position]
        holder.tvBikeName.text = "${bike.name} ${bike.model}"
        holder.tvEngineNo.text = "Engine No: ${bike.engNum}"
        holder.tvFreeService.text = "Reg: ${bike.reg}"

        // সিলেকশন স্টেট দেখানো
        holder.rbSelected.isChecked = position == selectedPosition

        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                updateSelectedBikeInFirebase(bikeList[currentPos], currentPos)
            }
        }
    }

    private fun updateSelectedBikeInFirebase(bike: Bike, position: Int) {
        val uid = auth.currentUser?.uid ?: return
        
        // selectedBike নোডে বাইকের ডাটা সেভ করা
        database.child(uid).child("selectedBike").setValue(bike)
            .addOnSuccessListener {
                val oldPos = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
            }
    }

    override fun getItemCount(): Int = bikeList.size

    // শুরুতে কোন বাইকটি সিলেক্ট করা আছে তা সেট করার জন্য (ঐচ্ছিক)
    fun setSelectedBike(bikeName: String?) {
        val index = bikeList.indexOfFirst { "${it.name} ${it.model}" == bikeName }
        if (index != -1) {
            selectedPosition = index
            notifyDataSetChanged()
        }
    }

    class MyBikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBikeName: TextView = itemView.findViewById(R.id.tvBikeName)
        val tvEngineNo: TextView = itemView.findViewById(R.id.tvEngineNo)
        val tvFreeService: TextView = itemView.findViewById(R.id.tvFreeService)
        val rbSelected: RadioButton = itemView.findViewById(R.id.rbSelected)
    }
}
