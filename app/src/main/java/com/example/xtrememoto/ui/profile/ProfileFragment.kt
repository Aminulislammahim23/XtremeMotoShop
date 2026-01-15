package com.example.xtrememoto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val user = auth.currentUser

        // Fetch User's Name from Firebase
        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(uid)
            userRef.child("Name").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && isAdded) {
                    tvUserName.text = snapshot.value.toString()
                }
            }
        }

        // Profile details navigation
        view.findViewById<MaterialCardView>(R.id.cvProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_viewProfileFragment)
        }

        // My Bikes navigation
        view.findViewById<MaterialCardView>(R.id.cvMyBikes).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myBikeFragment)
        }

        // Bike Documents navigation
        view.findViewById<MaterialCardView>(R.id.cvBikeDocuments).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_documentsFragment)
        }

        // Change Password navigation
        view.findViewById<MaterialCardView>(R.id.cvPassword).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_cPassFragment)
        }

        // Logout
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}
