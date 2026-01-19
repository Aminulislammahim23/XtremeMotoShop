package com.example.xtrememoto.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    private val bikePaths = mutableListOf<String>() // ডিলিট করার জন্য পাথ স্টোর করা
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
        val toolbar = view.findViewById<Toolbar>(R.id.adminBikesToolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rvBikes.layoutManager = LinearLayoutManager(context)
        adapter = AdminBikesAdapter(bikeList) { path ->
            deleteBike(path)
        }
        rvBikes.adapter = adapter

        fetchBikesFromAllCategories()

        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_myBikeFragment_to_addBikeFragment)
        }
    }

    private fun fetchBikesFromAllCategories() {
        // স্ট্রাকচার: shop -> bikes -> categories -> {catID} -> {catName} -> {bikeID}
        val bikeRef = database.getReference("shop/bikes/categories")
        bikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bikeList.clear()
                bikePaths.clear()
                
                for (catIdChild in snapshot.children) {
                    for (catNameChild in catIdChild.children) {
                        for (bikeChild in catNameChild.children) {
                            val bike = bikeChild.getValue(ShopBike::class.java)
                            bike?.let {
                                bikeList.add(it)
                                // ডিলিট করার জন্য সম্পূর্ণ পাথ রাখা হচ্ছে
                                bikePaths.add("shop/bikes/categories/${catIdChild.key}/${catNameChild.key}/${bikeChild.key}")
                            }
                        }
                    }
                }
                adapter.setPaths(bikePaths)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminBikes", "Error: ${error.message}")
            }
        })
    }

    private fun deleteBike(path: String) {
        database.getReference(path).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Bike Removed from Store", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class AdminBikesAdapter(
    private val list: List<ShopBike>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminBikesAdapter.ViewHolder>() {

    private var paths = listOf<String>()

    fun setPaths(newPaths: List<String>) {
        paths = newPaths
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: android.widget.TextView = view.findViewById(R.id.tvOfferTitle)
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bike = list[position]
        holder.tvTitle.text = bike.name
        holder.btnDelete.setOnClickListener { onDeleteClick(paths[position]) }
    }

    override fun getItemCount() = list.size
}
