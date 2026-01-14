package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R

class ServicePackageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_package, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val llExecutiveMore = view.findViewById<LinearLayout>(R.id.llExecutiveMore)
        val btnExecutiveViewMore = view.findViewById<Button>(R.id.btnExecutiveViewMore)
        btnExecutiveViewMore.setOnClickListener {
            if (llExecutiveMore.visibility == View.VISIBLE) {
                llExecutiveMore.visibility = View.GONE
                btnExecutiveViewMore.text = "View More"
            } else {
                llExecutiveMore.visibility = View.VISIBLE
                btnExecutiveViewMore.text = "View Less"
            }
        }

        val llPremiumMore = view.findViewById<LinearLayout>(R.id.llPremiumMore)
        val btnPremiumViewMore = view.findViewById<Button>(R.id.btnPremiumViewMore)
        btnPremiumViewMore.setOnClickListener {
            if (llPremiumMore.visibility == View.VISIBLE) {
                llPremiumMore.visibility = View.GONE
                btnPremiumViewMore.text = "View More"
            } else {
                llPremiumMore.visibility = View.VISIBLE
                btnPremiumViewMore.text = "View Less"
            }
        }

        val llSignatureMore = view.findViewById<LinearLayout>(R.id.llSignatureMore)
        val btnSignatureViewMore = view.findViewById<Button>(R.id.btnSignatureViewMore)
        btnSignatureViewMore.setOnClickListener {
            if (llSignatureMore.visibility == View.VISIBLE) {
                llSignatureMore.visibility = View.GONE
                btnSignatureViewMore.text = "View More"
            } else {
                llSignatureMore.visibility = View.VISIBLE
                btnSignatureViewMore.text = "View Less"
            }
        }
    }
}