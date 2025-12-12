package com.example.xtrememoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cvProfile = view.findViewById<MaterialCardView>(R.id.cvProfile)
        cvProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_viewProfileFragment)
        }

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        val cvContactUs = view.findViewById<MaterialCardView>(R.id.cvContactUs)
        cvContactUs.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_contactFragment)
        }
    }
}