package com.example.xtrememoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

class OnboardingFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivOnboardingImage = view.findViewById<ImageView>(R.id.ivOnboardingImage)
        ivOnboardingImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_onboarding_2))

        val tvOnboardingTitle = view.findViewById<TextView>(R.id.tvOnboardingTitle)
        tvOnboardingTitle.text = "Service Booking"

        val tvOnboardingDescription = view.findViewById<TextView>(R.id.tvOnboardingDescription)
        tvOnboardingDescription.text = "You can conveniently schedule your bike’s service appointments with just a few taps. Please choose the time and date that works best for you, and leave the rest to us."

        val tvSkip = view.findViewById<TextView>(R.id.tvSkip)
        tvSkip.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment2_to_loginFragment)
        }

        val btnNext = view.findViewById<ImageView>(R.id.btnNext)
        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment2_to_onboardingFragment3)
        }
    }
}