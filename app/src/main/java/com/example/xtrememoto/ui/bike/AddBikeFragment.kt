package com.example.xtrememoto.ui.bike

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Bike
import com.example.xtrememoto.viewmodel.BikeViewModel
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class AddBikeFragment : Fragment() {

    private lateinit var viewModel: BikeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_bike, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BikeViewModel::class.java]

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etModel = view.findViewById<EditText>(R.id.etModel)
        val etColor = view.findViewById<EditText>(R.id.etColor)
        val etCustName = view.findViewById<EditText>(R.id.etCustName)
        val etEngNum = view.findViewById<EditText>(R.id.etEngNum)
        val etFrameNum = view.findViewById<EditText>(R.id.etFrameNum)
        val etPurchase = view.findViewById<EditText>(R.id.etPurchase)
        val etReg = view.findViewById<EditText>(R.id.etReg)
        val btnAddBike = view.findViewById<MaterialButton>(R.id.btnAddBike)

        // Force all input to uppercase
        val allCapsFilter = arrayOf(InputFilter.AllCaps())
        listOf(etName, etModel, etColor, etCustName, etEngNum, etFrameNum, etReg).forEach {
            it.filters = allCapsFilter
        }

        observeViewModel(etCustName)

        viewModel.fetchUserName()

        btnBack.setOnClickListener { findNavController().popBackStack() }

        etPurchase.setOnClickListener { showDatePicker(etPurchase) }

        btnAddBike.setOnClickListener {
            val bike = Bike(
                name = etName.text.toString().trim(),
                model = etModel.text.toString().trim(),
                color = etColor.text.toString().trim(),
                custName = etCustName.text.toString().trim(),
                engNum = etEngNum.text.toString().trim(),
                frameNum = etFrameNum.text.toString().trim(),
                purchase = etPurchase.text.toString().trim(),
                reg = etReg.text.toString().trim()
            )

            if (validateBike(bike)) {
                viewModel.addBike(bike)
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel(etCustName: EditText) {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            name?.let { etCustName.setText(it.uppercase()) }
        }

        viewModel.addBikeStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is BikeViewModel.BikeStatus.Loading -> { /* Show Loading */ }
                is BikeViewModel.BikeStatus.Success -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is BikeViewModel.BikeStatus.Error -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                editText.setText("$year-${month + 1}-$day")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateBike(bike: Bike): Boolean {
        return !bike.name.isNullOrEmpty() && !bike.model.isNullOrEmpty() &&
                !bike.color.isNullOrEmpty() && !bike.engNum.isNullOrEmpty() &&
                !bike.frameNum.isNullOrEmpty() && !bike.reg.isNullOrEmpty()
    }
}