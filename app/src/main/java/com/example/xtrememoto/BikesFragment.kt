package com.example.xtrememoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class BikesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bikes, container, false)

        // Set a click listener on the root view to navigate to the bike detail screen
        view.setOnClickListener {
            findNavController().navigate(R.id.action_bikesFragment_to_bikeDetailFragment)
        }

        return view
    }
}