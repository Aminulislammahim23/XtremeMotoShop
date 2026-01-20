package com.example.xtrememoto.ui.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File

class AdminFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvAdminName: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        ivProfileImage = view.findViewById(R.id.ivAdminProfileImage)
        tvAdminName = view.findViewById(R.id.tvAdminName)

        loadAdminData()

        // Navigation
        view.findViewById<MaterialCardView>(R.id.cvManageOffers).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminOffersFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvManageBikes).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminBikesFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvManageParts).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminPartsFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvManageBookings).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminBookingFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvAllBookings).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminBookingFragment)
        }

        // Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_adminProfileFragment)
        }

        // Logout
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_adminFragment_to_loginFragment)
        }
    }

    private fun loadAdminData() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                if (snapshot.exists()) {
                    val name = snapshot.child("Name").value?.toString() ?: "Admin"
                    val profilePicPath = snapshot.child("profilePic").value?.toString() ?: ""

                    tvAdminName.text = name

                    if (profilePicPath.isNotEmpty()) {
                        val imgFile = File(profilePicPath)
                        if (imgFile.exists()) {
                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            ivProfileImage.setImageBitmap(myBitmap)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
