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
            checkUserSession()
        }, 2000) // 2 second delay
    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // যদি ইউজার লগইন করা থাকে, সরাসরি HomeFragment-এ নিয়ে যাবে
            // নিশ্চিত করুন আপনার nav_graph-এ এই অ্যাকশনটি আছে অথবা সরাসরি ID ব্যবহার করুন
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        } else {
            // লগইন করা না থাকলে Onboarding-এ পাঠাবে
            findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment1)
        }
    }
}
