package com.example.xtrememoto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CPassFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_c_pass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val etCurrentPass = view.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPass = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPass = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnUpdate = view.findViewById<MaterialButton>(R.id.btnUpdate)

        // Fetch user name
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            database.getReference("users").child(uid).child("Name").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists() && isAdded) {
                        tvUserName.text = snapshot.value.toString()
                    }
                }
        }

        btnBack.setOnClickListener { findNavController().popBackStack() }
        btnCancel.setOnClickListener { findNavController().popBackStack() }

        btnUpdate.setOnClickListener {
            val currentPass = etCurrentPass.text.toString()
            val newPass = etNewPass.text.toString()
            val confirmPass = etConfirmPass.text.toString()

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePassword(currentPass, newPass)
        }
    }

    private fun updatePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            // Re-authenticate user before updating password
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(context, "Error: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
