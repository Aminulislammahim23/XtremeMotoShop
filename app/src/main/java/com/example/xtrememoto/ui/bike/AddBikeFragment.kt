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
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AddBikeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_bike, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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

        // Force all input to uppercase as you type
        val allCapsFilter = arrayOf(InputFilter.AllCaps())
        etName.filters = allCapsFilter
        etModel.filters = allCapsFilter
        etColor.filters = allCapsFilter
        etCustName.filters = allCapsFilter
        etEngNum.filters = allCapsFilter
        etFrameNum.filters = allCapsFilter
        etReg.filters = allCapsFilter

        // Fetch User's Name from Firebase for etCustName
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(uid)
            userRef.child("Name").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && isAdded) {
                    etCustName.setText(snapshot.value.toString().uppercase())
                }
            }
        }

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

                saveBikeData(name, model, color, custName, engNum, frameNum, purchase, reg)
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveBikeData(
        name: String, model: String, color: String, custName: String,
        engNum: String, frameNum: String, purchase: String, reg: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val bikesRef = database.getReference("users").child(userId).child("Bikes")

        bikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nextIndex = (snapshot.childrenCount + 1).toString()

                val bikeData = mapOf(
                    "name" to name,
                    "model" to model,
                    "color" to color,
                    "custName" to custName,
                    "engNum" to engNum,
                    "frameNum" to frameNum,
                    "purchase" to purchase,
                    "reg" to reg
                )

                bikesRef.child(nextIndex).setValue(bikeData)
                    .addOnSuccessListener {
                        if (isAdded) {
                            Toast.makeText(context, "Bike Added Successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                    .addOnFailureListener { e ->
                        if (isAdded) {
                            Toast.makeText(context, "Failed to add bike: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}