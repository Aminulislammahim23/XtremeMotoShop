package com.example.xtrememoto.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvBikeName = view.findViewById<TextView>(R.id.tvBikeName)
        val user = auth.currentUser

        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(uid)
            
            // ইউজারের নাম নিয়ে আসা
            userRef.child("Name").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    tvUserName.text = snapshot.value.toString()
                }
            }

            // সিলেক্টেড বাইকের নাম নিয়ে আসা (রিয়েল-টাইম আপডেট)
            userRef.child("selectedBike").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value?.toString() ?: ""
                        val model = snapshot.child("model").value?.toString() ?: ""
                        tvBikeName.text = "$name $model".uppercase()
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        }

        view.findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        view.findViewById<Button>(R.id.btnChange).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myBikeFragment)
        }

        setupNavigation(view)
        setupExpandCollapse(view)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<MaterialCardView>(R.id.cvBikeDocs).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_documentsFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServiceHistory).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceHistoryFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServicePackages).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_servicePackageFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvTestRide).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bookTestRideFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServiceBooking).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceBookingFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvParts).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_partsFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvBikes).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bikesFragment)
        }
    }

    private fun setupExpandCollapse(view: View) {
        val ivExpandCollapse = view.findViewById<ImageView>(R.id.buttonExp)
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
    }
}
