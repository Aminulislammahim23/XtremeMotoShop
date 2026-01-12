package com.example.xtrememoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.adapter.MyBikeAdapter
import com.google.android.material.button.MaterialButton

class MyBikeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_bike, container, false)

        // Setup RecyclerView
        val rvMyBikes = view.findViewById<RecyclerView>(R.id.rvMyBikes)
        rvMyBikes.layoutManager = LinearLayoutManager(context)
        rvMyBikes.adapter = MyBikeAdapter()

        // Setup Back Button
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
        
        // Setup Add New Bike Button (XML এর আইডির সাথে ম্যাচ করানো হয়েছে)
        val btnAddNewBike = view.findViewById<MaterialButton>(R.id.btnAddNewBike)
        btnAddNewBike.setOnClickListener {
            findNavController().navigate(R.id.action_myBikeFragment_to_addBikeFragment)
        }

        return view
    }
}
