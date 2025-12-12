package com.example.xtrememoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

class OnboardingFragment3 : Fragment() {

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
        ivOnboardingImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_onboarding_3))

        val tvOnboardingTitle = view.findViewById<TextView>(R.id.tvOnboardingTitle)
        tvOnboardingTitle.text = "Easy to Shop"

        val tvOnboardingDescription = view.findViewById<TextView>(R.id.tvOnboardingDescription)
        tvOnboardingDescription.text = "Explore the latest Suzuki bikes and accessories with ease. Browse, compare, and shop right from your phone to get the ride of your dreams."

        val tvSkip = view.findViewById<TextView>(R.id.tvSkip)
        tvSkip.visibility = View.GONE

        val btnNext = view.findViewById<ImageView>(R.id.btnNext)
        btnNext.visibility = View.GONE

        val btnLetsStart = view.findViewById<Button>(R.id.btnLetsStart)
        btnLetsStart.visibility = View.VISIBLE
        btnLetsStart.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment3_to_loginFragment)
        }
    }
}