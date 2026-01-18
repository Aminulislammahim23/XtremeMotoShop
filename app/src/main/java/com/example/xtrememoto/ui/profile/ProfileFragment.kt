package com.example.xtrememoto.ui.profile

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
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ivProfileImage: ImageView

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
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        val user = auth.currentUser

        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(uid)
            
            // ইউজারের নাম এবং প্রোফাইল পিকচার পাথ ফেচ করা হচ্ছে
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && isAdded) {
                    val name = snapshot.child("Name").value?.toString() ?: "User"
                    val profilePicPath = snapshot.child("profilePic").value?.toString() ?: ""
                    
                    tvUserName.text = name

                    // যদি লোকাল পাথ থাকে তবে ছবি লোড করো
                    if (profilePicPath.isNotEmpty()) {
                        val imgFile = File(profilePicPath)
                        if (imgFile.exists()) {
                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            ivProfileImage.setImageBitmap(myBitmap)
                        }
                    }
                }
            }
        }

        // নেভিগেশন লজিক
        view.findViewById<MaterialCardView>(R.id.cvProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_viewProfileFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvMyBikes).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myBikeFragment)
        }

        view.findViewById<MaterialCardView>(R.id.cvBikeDocuments).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_documentsFragment)
        }

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
