package com.example.xtrememoto

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class AddBikeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_bike, container, false)

        // Find views
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

        // Setup Back Button
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup Date Picker for Purchase Date
        etPurchase.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    etPurchase.setText(date)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Setup Add Bike Button
        btnAddBike.setOnClickListener {
            val name = etName.text.toString().trim()
            val model = etModel.text.toString().trim()
            val color = etColor.text.toString().trim()
            val custName = etCustName.text.toString().trim()
            val engNum = etEngNum.text.toString().trim()
            val frameNum = etFrameNum.text.toString().trim()
            val purchase = etPurchase.text.toString().trim()
            val reg = etReg.text.toString().trim()

            if (name.isNotEmpty() && model.isNotEmpty() && color.isNotEmpty() && 
                custName.isNotEmpty() && engNum.isNotEmpty() && frameNum.isNotEmpty() && 
                purchase.isNotEmpty() && reg.isNotEmpty()) {
                
                // Logic to save bike data to Firebase would go here
                Toast.makeText(context, "Bike Added Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}