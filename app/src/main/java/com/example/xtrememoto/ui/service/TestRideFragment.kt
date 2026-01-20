package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R

class TestRideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_ride, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val btnBookTestRide = view.findViewById<Button>(R.id.btnBookTestRide)
        btnBookTestRide.setOnClickListener {
            findNavController().navigate(R.id.action_testRideFragment_to_bookTestRideFragment)
        }
    }
}
