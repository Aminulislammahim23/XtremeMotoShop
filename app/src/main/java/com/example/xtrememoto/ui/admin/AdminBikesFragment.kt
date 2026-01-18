package com.example.xtrememoto.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.ShopBike
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AdminBikesFragment : Fragment() {

    private lateinit var rvBikes: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val bikeList = mutableListOf<ShopBike>()
    private val bikeKeys = mutableListOf<String>()
    private lateinit var adapter: AdminBikesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_bikes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvBikes = view.findViewById(R.id.rvAdminBikes)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddBike)

        rvBikes.layoutManager = LinearLayoutManager(context)
        adapter = AdminBikesAdapter(bikeList) { key ->
            deleteBike(key)
        }
        rvBikes.adapter = adapter

        fetchBikes()

        fabAdd.setOnClickListener {
            // logic to show add bike dialog
            Toast.makeText(context, "Add Bike Dialog Placeholder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBikes() {
        val bikeRef = database.getReference("shop/bikes")
        bikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bikeList.clear()
                bikeKeys.clear()
                for (child in snapshot.children) {
                    val bike = child.getValue(ShopBike::class.java)
                    bike?.let {
                        bikeList.add(it)
                        bikeKeys.add(child.key ?: "")
                    }
                }
                adapter.setKeys(bikeKeys)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun deleteBike(key: String) {
        database.getReference("shop/bikes").child(key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Bike Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class AdminBikesAdapter(
    private val list: List<ShopBike>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminBikesAdapter.ViewHolder>() {

    private var keys = listOf<String>()

    fun setKeys(newKeys: List<String>) {
        keys = newKeys
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: android.widget.TextView = view.findViewById(R.id.tvOfferTitle) // reuse item_admin_offer for simplicity or create item_admin_bike
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = list[position].name
        holder.btnDelete.setOnClickListener { onDeleteClick(keys[position]) }
    }

    override fun getItemCount() = list.size
}
