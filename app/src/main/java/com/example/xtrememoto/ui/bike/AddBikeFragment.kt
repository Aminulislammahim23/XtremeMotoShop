package com.example.xtrememoto.ui.bike

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Bike
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddBikeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_bike, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etModel = view.findViewById<TextInputEditText>(R.id.etModel)
        val etColor = view.findViewById<TextInputEditText>(R.id.etColor)
        val etReg = view.findViewById<TextInputEditText>(R.id.etReg)
        val etEngNum = view.findViewById<TextInputEditText>(R.id.etEngNum)
        val etFrameNum = view.findViewById<TextInputEditText>(R.id.etFrameNum)
        val etPurchase = view.findViewById<TextInputEditText>(R.id.etPurchase)
        val etCustName = view.findViewById<TextInputEditText>(R.id.etCustName)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveBike)

        // Fetch and set customer name
        fetchCustomerName(etCustName)

        btnBack?.setOnClickListener { findNavController().popBackStack() }

        btnSave?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val model = etModel?.text?.toString()?.trim() ?: ""
            val color = etColor?.text?.toString()?.trim() ?: ""
            val reg = etReg?.text?.toString()?.trim() ?: ""
            val engNum = etEngNum?.text?.toString()?.trim() ?: ""
            val frameNum = etFrameNum?.text?.toString()?.trim() ?: ""
            val purchase = etPurchase?.text?.toString()?.trim() ?: ""
            val custName = etCustName?.text?.toString()?.trim() ?: ""

            if (name.isEmpty() || model.isEmpty() || reg.isEmpty()) {
                Toast.makeText(context, "Name, Model and Reg are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bike = Bike(
                name = name,
                model = model,
                color = color,
                reg = reg,
                engNum = engNum,
                frameNum = frameNum,
                purchase = purchase,
                custName = custName
            )

            saveBikeToUser(bike)
        }
    }

    private fun fetchCustomerName(etCustName: TextInputEditText?) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("Name").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists() && isAdded) {
                    etCustName?.setText(snapshot.value?.toString())
                }
            }
    }

    private fun saveBikeToUser(bike: Bike) {
        val uid = auth.currentUser?.uid ?: return
        val userBikesRef = database.getReference("users").child(uid).child("Bikes")
        
        val bikeKey = userBikesRef.push().key ?: return
        userBikesRef.child(bikeKey).setValue(bike).addOnCompleteListener {
            if (it.isSuccessful && isAdded) {
                Toast.makeText(context, "Bike Added Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Failed to add bike", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
