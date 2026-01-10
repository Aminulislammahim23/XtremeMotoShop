package com.example.xtrememoto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)

        // Fetch and display the user's name
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val docRef = db.collection("users").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fullName = document.getString("fullName")
                        if (fullName != null) {
                            tvUserName.text = fullName
                        } else {
                            tvUserName.text = "Welcome User"
                        }
                    } else {
                        tvUserName.text = "Welcome"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("HomeFragment", "get failed with ", exception)
                    tvUserName.text = "Welcome"
                }
        } else {
            tvUserName.text = "Welcome"
        }

        val logoutButton = view.findViewById<ImageButton>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        val cvServiceHistory = view.findViewById<MaterialCardView>(R.id.cvServiceHistory)
        cvServiceHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceHistoryFragment)
        }

        val cvBikeDocs = view.findViewById<MaterialCardView>(R.id.cvBikeDocs)
        cvBikeDocs.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_documentsFragment)
        }

        val cvServicePackages = view.findViewById<MaterialCardView>(R.id.cvServicePackages)
        cvServicePackages.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_servicePackageFragment)
        }

        val cvTestRide = view.findViewById<MaterialCardView>(R.id.cvTestRide)
        cvTestRide.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_testRideFragment)
        }

        val cvServiceBooking = view.findViewById<MaterialCardView>(R.id.cvServiceBooking)
        cvServiceBooking.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceBookingFragment)
        }

        val cvBikes = view.findViewById<MaterialCardView>(R.id.cvBikes)
        cvBikes.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bikesFragment)
        }

        val cvParts = view.findViewById<MaterialCardView>(R.id.cvParts)
        cvParts.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_partsFragment)
        }
    }
}
