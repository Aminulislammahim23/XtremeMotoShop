package com.example.xtrememoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ViewProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private var oldName = ""
    private var oldPhone = ""
    private var oldEmail = ""
    private var oldGender = ""
    private var oldDob = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val etFullName = view.findViewById<TextInputEditText>(R.id.tilFullname)
        val etPhone = view.findViewById<TextInputEditText>(R.id.tilPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.tilEmail)
        val actGender = view.findViewById<AutoCompleteTextView>(R.id.actGender)
        val etDob = view.findViewById<TextInputEditText>(R.id.tilDob)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // জেন্ডার অপশন সেট করা
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        actGender.setAdapter(adapter)

        val inputFields = listOf(etFullName, etPhone, etEmail, actGender, etDob)

        // ডিফল্ট স্টেট
        inputFields.forEach { it.isEnabled = false }
        btnSave.visibility = View.GONE

        val user = auth.currentUser
        val uid = user?.uid

        uid?.let {
            val userRef = database.getReference("users").child(it)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && isAdded) {
                    oldName = snapshot.child("Name").value?.toString() ?: ""
                    oldPhone = snapshot.child("Phone").value?.toString() ?: ""
                    oldEmail = snapshot.child("Email").value?.toString() ?: ""
                    oldGender = snapshot.child("Gender").value?.toString() ?: ""
                    oldDob = snapshot.child("DOB").value?.toString() ?: ""

                    tvUserName.text = oldName
                    etFullName.setText(oldName)
                    etPhone.setText(oldPhone)
                    etEmail.setText(oldEmail)
                    actGender.setText(oldGender, false)
                    etDob.setText(oldDob)
                }
            }
        }

        btnEdit.setOnClickListener {
            inputFields.forEach { it.isEnabled = true }
            btnSave.visibility = View.VISIBLE
            etFullName.requestFocus()
        }

        btnSave.setOnClickListener {
            val newName = etFullName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newGender = actGender.text.toString().trim()
            val newDob = etDob.text.toString().trim()

            if (newName == oldName && newPhone == oldPhone && newEmail == oldEmail && 
                newGender == oldGender && newDob == oldDob) {
                
                Toast.makeText(context, "No changes detected", Toast.LENGTH_SHORT).show()
                inputFields.forEach { it.isEnabled = false }
                btnSave.visibility = View.GONE
            } else {
                uid?.let { userId ->
                    val userRef = database.getReference("users").child(userId)
                    val updates = hashMapOf<String, Any>(
                        "Name" to newName,
                        "Phone" to newPhone,
                        "Email" to newEmail,
                        "Gender" to newGender,
                        "DOB" to newDob
                    )

                    userRef.updateChildren(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful && isAdded) {
                            Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                            oldName = newName
                            oldPhone = newPhone
                            oldEmail = newEmail
                            oldGender = newGender
                            oldDob = newDob
                            tvUserName.text = newName
                            inputFields.forEach { it.isEnabled = false }
                            btnSave.visibility = View.GONE
                        } else {
                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_viewProfileFragment_to_profileFragment)
        }
    }
}
