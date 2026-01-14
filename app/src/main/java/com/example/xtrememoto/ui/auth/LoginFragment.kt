package com.example.xtrememoto.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading or disable button here if needed
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        if (uid != null) {
                            val database = FirebaseDatabase.getInstance().getReference("users")

                            // Store last login timestamp
                            database.child(uid).child("lastLogin").setValue(System.currentTimeMillis())
                                .addOnCompleteListener { dbTask ->
                                    if (isAdded) { // Check if fragment is still attached
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                        } else {
                                            // Even if DB fails, we proceed to Home since Auth was successful
                                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                        }
                                    }
                                }
                        }
                    } else {
                        if (isAdded) {
                            btnLogin.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)
        tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }
}