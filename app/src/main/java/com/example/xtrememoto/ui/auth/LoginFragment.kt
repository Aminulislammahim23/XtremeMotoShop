package com.example.xtrememoto.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.example.xtrememoto.viewmodel.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var etEmail: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        etEmail = view.findViewById(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)

        observeViewModel(btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        tvForgotPassword.setOnClickListener {
            showForgotPasswordBottomSheet()
        }

        tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun showForgotPasswordBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        
        val etResetEmail = view.findViewById<TextInputEditText>(R.id.etResetEmail)
        val btnSend = view.findViewById<Button>(R.id.btnSendResetLink)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)

        val currentEmail = etEmail.text.toString().trim()
        etResetEmail.setText(currentEmail)

        btnSend.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.resetPassword(email)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun observeViewModel(btnLogin: Button) {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    btnLogin.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    btnLogin.isEnabled = true
                    if (state.message == "Login Successful") {
                        checkUserRoleAndNavigate()
                    } else {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is AuthViewModel.AuthState.Error -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkUserRoleAndNavigate() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.child("role").get().addOnSuccessListener { snapshot ->
            if (isAdded) {
                val role = snapshot.value?.toString() ?: "customer"
                if (role == "admin") {
                    // অ্যাডমিন হলে AdminFragment-এ নিয়ে যাবে
                    // নিশ্চিত করুন nav_graph-এ এই অ্যাকশনটি আছে
                    findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                } else {
                    // কাস্টমার হলে HomeFragment-এ নিয়ে যাবে
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                // কোনো কারণে রোল চেক ব্যর্থ হলে ডিফল্টভাবে হোমে নিয়ে যাবে
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }
}
