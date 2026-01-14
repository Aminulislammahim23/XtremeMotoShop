package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R

class BookTestRideFragment : Fragment() {

    private var currentStep = 1
    private lateinit var stepContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_test_ride, container, false)
        stepContainer = view.findViewById(R.id.step_container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStep(1)

        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateStep(step: Int) {
        currentStep = step
        val inflater = LayoutInflater.from(requireContext())
        stepContainer.removeAllViews()

        when (step) {
            1 -> {
                val step1View = inflater.inflate(R.layout.step_choose_motorcycle, stepContainer, false)
                stepContainer.addView(step1View)

                step1View.findViewById<View>(R.id.card_gixxer).setOnClickListener {
                    updateStep(2)
                }
            }
            2 -> {
                val step2View = inflater.inflate(R.layout.step_find_dealer, stepContainer, false)
                stepContainer.addView(step2View)

                val divisions = arrayOf("Barishal", "Chattogram", "Dhaka", "Khulna", "Mymensingh", "Rajshahi", "Rangpur", "Sylhet")
                val divisionAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    divisions
                )
                step2View.findViewById<AutoCompleteTextView>(R.id.division_dropdown).setAdapter(divisionAdapter)

                val districts = arrayOf("Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail")
                val districtAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    districts
                )
                step2View.findViewById<AutoCompleteTextView>(R.id.district_dropdown).setAdapter(districtAdapter)

                step2View.findViewById<Button>(R.id.btn_change).setOnClickListener {
                    updateStep(1)
                }

                step2View.findViewById<Button>(R.id.btn_search).setOnClickListener {
                   updateStep(3)
                }
            }
            3 -> {
                val step3View = inflater.inflate(R.layout.step_enter_details, stepContainer, false)
                stepContainer.addView(step3View)

                step3View.findViewById<Button>(R.id.btn_submit).setOnClickListener {
                    findNavController().popBackStack()
                }
            }
        }
    }
}