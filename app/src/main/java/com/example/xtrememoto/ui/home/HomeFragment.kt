package com.example.xtrememoto.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

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
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val user = auth.currentUser

        // ইউজার লগইন করা থাকলে তার নাম ডাটাবেস থেকে নিয়ে আসা
        user?.let {
            val uid = it.uid
            // আপনার ইমেজ অনুযায়ী পাথ হলো "users" এবং কি হলো "Name"
            val userRef = database.getReference("users").child(uid)
            userRef.child("Name").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    tvUserName.text = snapshot.value.toString()
                }
            }
        }

        val logoutButton = view.findViewById<ImageButton>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            auth.signOut() // Firebase থেকে সাইন আউট করা
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

        val ivExpandCollapse = view.findViewById<ImageView>(R.id.ivExpandCollapse)
        val expandableLayout = view.findViewById<LinearLayout>(R.id.expandable_layout)

        ivExpandCollapse.setOnClickListener {
            if (expandableLayout.visibility == View.VISIBLE) {
                expandableLayout.visibility = View.GONE
                ivExpandCollapse.setImageResource(R.drawable.ic_expand_more)
            } else {
                expandableLayout.visibility = View.VISIBLE
                ivExpandCollapse.setImageResource(R.drawable.ic_expand_less)
            }
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