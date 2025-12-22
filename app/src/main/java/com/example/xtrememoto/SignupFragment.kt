package com.example.xtrememoto

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etMobileNumber = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnSignUp = view.findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val fullName = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etMobileNumber.text.toString()
            val password = etPassword.text.toString()

            if (fullName.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid
                            if (userId != null) {
                                val userRef = database.getReference("users").child(userId)
                                val userData = hashMapOf(
                                    "fullName" to fullName,
                                    "email" to email,
                                    "phone" to phone
                                )
                                userRef.setValue(userData).addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Toast.makeText(requireContext(), "Signup Successfully", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)
        tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}