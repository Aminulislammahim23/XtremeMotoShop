package com.example.xtrememoto.ui.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSessionAndRole()
        }, 2000) // 2 second delay
    }

    private fun checkUserSessionAndRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

            // ডাটাবেস থেকে ইউজারের রোল রিড করা হচ্ছে
            userRef.child("role").get().addOnSuccessListener { snapshot ->
                if (isAdded) {
                    val role = snapshot.value?.toString() ?: "customer"
                    if (role == "admin") {
                        // যদি রোল admin হয় তবে সরাসরি AdminFragment এ যাবে
                        findNavController().navigate(R.id.action_splashFragment_to_adminFragment)
                    } else {
                        // যদি রোল customer হয় তবে সরাসরি HomeFragment এ যাবে
                        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    }
                }
            }.addOnFailureListener {
                if (isAdded) {
                    // এরর হলে ডিফল্টভাবে হোমে পাঠাবে
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
            }
        } else {
            // লগইন করা না থাকলে অনবোর্ডিং এ পাঠাবে
            findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment1)
        }
    }
}
