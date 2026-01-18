package com.example.xtrememoto.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ViewProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnEditProfileImage: FloatingActionButton
    private var selectedImageUri: Uri? = null
    private var isEditMode = false
    private var localImagePath: String = ""

    private var oldName = ""
    private var oldPhone = ""
    private var oldEmail = ""
    private var oldGender = ""
    private var oldDob = ""

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                selectedImageUri = it
                ivProfileImage.setImageURI(it)
                // ছবি লোকাল স্টোরেজে সেভ করা এবং পাথ সংগ্রহ করা
                localImagePath = saveImageToInternalStorage(it)
            }
        }
    }

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
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        btnEditProfileImage = view.findViewById(R.id.btnEditProfileImage)

        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        actGender.setAdapter(adapter)

        val inputFields = listOf(etFullName, etPhone, etEmail, actGender, etDob)

        inputFields.forEach { it.isEnabled = false }
        btnSave.visibility = View.GONE
        btnEditProfileImage.visibility = View.GONE

        loadUserProfile(tvUserName, etFullName, etPhone, etEmail, actGender, etDob)

        btnEditProfileImage.setOnClickListener {
            openGallery()
        }

        btnEdit.setOnClickListener {
            isEditMode = true
            inputFields.forEach { it.isEnabled = true }
            btnSave.visibility = View.VISIBLE
            btnEditProfileImage.visibility = View.VISIBLE
            etFullName.requestFocus()
        }

        btnSave.setOnClickListener {
            saveProfileUpdates(uid = auth.currentUser?.uid, etFullName, etPhone, etEmail, actGender, etDob, btnSave, inputFields, tvUserName)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadUserProfile(tvUserName: TextView, etFullName: EditText, etPhone: EditText, etEmail: EditText, actGender: AutoCompleteTextView, etDob: EditText) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && isAdded) {
                oldName = snapshot.child("Name").value?.toString() ?: ""
                oldPhone = snapshot.child("Phone").value?.toString() ?: ""
                oldEmail = snapshot.child("Email").value?.toString() ?: ""
                oldGender = snapshot.child("Gender").value?.toString() ?: ""
                oldDob = snapshot.child("DOB").value?.toString() ?: ""
                val profilePicPath = snapshot.child("profilePic").value?.toString() ?: ""

                tvUserName.text = oldName
                etFullName.setText(oldName)
                etPhone.setText(oldPhone)
                etEmail.setText(oldEmail)
                actGender.setText(oldGender, false)
                etDob.setText(oldDob)

                // যদি লোকাল পাথ থাকে তবে ছবি লোড করো
                if (profilePicPath.isNotEmpty()) {
                    val imgFile = File(profilePicPath)
                    if (imgFile.exists()) {
                        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        ivProfileImage.setImageBitmap(myBitmap)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val context = requireContext()
        val folder = File(context.filesDir, "profile_images")
        if (!folder.exists()) folder.mkdirs()

        val fileName = "profile_${auth.currentUser?.uid}.jpg"
        val file = File(folder, fileName)

        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("ViewProfile", "Error saving image: ${e.message}")
        }
        return ""
    }

    private fun saveProfileUpdates(uid: String?, etFullName: EditText, etPhone: EditText, etEmail: EditText, actGender: AutoCompleteTextView, etDob: EditText, btnSave: Button, inputFields: List<View>, tvUserName: TextView) {
        val newName = etFullName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        val newGender = actGender.text.toString().trim()
        val newDob = etDob.text.toString().trim()

        uid?.let { userId ->
            val userRef = database.getReference("users").child(userId)
            val updates = hashMapOf<String, Any>(
                "Name" to newName,
                "Phone" to newPhone,
                "Email" to newEmail,
                "Gender" to newGender,
                "DOB" to newDob
            )

            // যদি নতুন ছবি সিলেক্ট করা হয় তবে পাথ আপডেট করো
            if (localImagePath.isNotEmpty()) {
                updates["profilePic"] = localImagePath
            }

            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful && isAdded) {
                    Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    isEditMode = false
                    oldName = newName
                    tvUserName.text = newName
                    inputFields.forEach { it.isEnabled = false }
                    btnSave.visibility = View.GONE
                    btnEditProfileImage.visibility = View.GONE
                }
            }
        }
    }
}
