package com.example.xtrememoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.adapter.MyBikeAdapter
import com.example.xtrememoto.model.Bike
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyBikeFragment : Fragment() {

    private lateinit var rvMyBikes: RecyclerView
    private lateinit var adapter: MyBikeAdapter
    private val bikeList = ArrayList<Bike>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_bike, container, false)

        // Find views
        rvMyBikes = view.findViewById(R.id.rvMyBikes)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnAddNewBike = view.findViewById<MaterialButton>(R.id.btnAddNewBike)

        // Setup RecyclerView
        adapter = MyBikeAdapter(bikeList)
        rvMyBikes.adapter = adapter
        rvMyBikes.layoutManager = LinearLayoutManager(requireContext())

        // Fetch Data from Firebase
        fetchBikesFromFirebase()

        // Setup Back Button
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // Setup Add New Bike Button
        btnAddNewBike.setOnClickListener {
            findNavController().navigate(R.id.action_myBikeFragment_to_addBikeFragment)
        }

        return view
    }

    private fun fetchBikesFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("Bikes")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bikeList.clear()

                if (snapshot.exists()) {
                    for (bikeSnap in snapshot.children) {
                        val bike = bikeSnap.getValue(Bike::class.java)
                        if (bike != null) {
                            bikeList.add(bike)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
