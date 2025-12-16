package com.example.xtrememoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutButton = view.findViewById<ImageButton>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        val cvBikeDocs = view.findViewById<MaterialCardView>(R.id.cvBikeDocs)
        cvBikeDocs.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_documentsFragment)
        }

        val cvServiceHistory = view.findViewById<MaterialCardView>(R.id.cvServiceHistory)
        cvServiceHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceHistoryFragment)
        }

        val cvServicePackages = view.findViewById<MaterialCardView>(R.id.cvServicePackages)
        cvServicePackages.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_servicePackageFragment)
        }

        val cvTestRide = view.findViewById<MaterialCardView>(R.id.cvTestRide)
        cvTestRide.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bookTestRideFragment)
        }

        val cvServiceBooking = view.findViewById<MaterialCardView>(R.id.cvServiceBooking)
        cvServiceBooking.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceBookingFragment)
        }

        val cvParts = view.findViewById<MaterialCardView>(R.id.cvParts)
        cvParts.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_partsFragment)
        }

        val cvBikes = view.findViewById<MaterialCardView>(R.id.cvBikes)
        cvBikes.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bikesFragment)
        }
    }
}