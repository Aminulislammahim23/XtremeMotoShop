package com.example.xtrememoto.ui.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.databinding.FragmentAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File

class AdminProfileFragment : Fragment() {

    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadAdminProfile()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnUpdateProfile.setOnClickListener {
            updateAdminProfile()
        }
    }

    private fun loadAdminProfile() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && isAdded) {
                binding.etAdminName.setText(snapshot.child("Name").value?.toString())
                binding.etShopAddress.setText(snapshot.child("Address").value?.toString())
                binding.etAdminPhone.setText(snapshot.child("Phone").value?.toString())

                val profilePicPath = snapshot.child("profilePic").value?.toString() ?: ""
                if (profilePicPath.isNotEmpty()) {
                    val imgFile = File(profilePicPath)
                    if (imgFile.exists()) {
                        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        binding.ivAdminProfilePic.setImageBitmap(myBitmap)
                    }
                }
            }
        }
    }

    private fun updateAdminProfile() {
        val uid = auth.currentUser?.uid ?: return
        val name = binding.etAdminName.text.toString().trim()
        val address = binding.etShopAddress.text.toString().trim()
        val phone = binding.etAdminPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "Name" to name,
            "Address" to address,
            "Phone" to phone
        )

        database.getReference("users").child(uid).updateChildren(updates)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener {
                if (isAdded) Toast.makeText(context, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
