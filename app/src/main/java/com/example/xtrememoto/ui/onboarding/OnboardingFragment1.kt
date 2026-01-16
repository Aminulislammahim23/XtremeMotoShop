package com.example.xtrememoto.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R

class OnboardingFragment1 : Fragment() {

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
        ivOnboardingImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_onboarding_1))

        val tvOnboardingTitle = view.findViewById<TextView>(R.id.tvOnboardingTitle)
        tvOnboardingTitle.text = "Bike Servicing"

        val tvOnboardingDescription = view.findViewById<TextView>(R.id.tvOnboardingDescription)
        tvOnboardingDescription.text = "Keep your Suzuki bike in top-notch condition with our expert servicing options. Book maintenance, check service details, and ensure your bike delivers peak performance every time."

        val tvSkip = view.findViewById<TextView>(R.id.tvSkip)
        tvSkip.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment1_to_loginFragment)
        }

        val btnNext = view.findViewById<ImageView>(R.id.btnBook)
        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment1_to_onboardingFragment2)
        }
    }
}