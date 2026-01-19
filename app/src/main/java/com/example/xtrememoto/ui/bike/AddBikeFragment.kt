package com.example.xtrememoto.ui.bike

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.example.xtrememoto.model.BikeSpecs
import com.example.xtrememoto.model.ShopBike
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddBikeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var ivBikePreview: ImageView? = null
    private var localImagePath: String = ""

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                ivBikePreview?.setImageURI(uri)
                ivBikePreview?.alpha = 1.0f
                localImagePath = saveBikeImageLocally(uri)
            }
        }
    }

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

        checkAdminAccess()

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val cvBikeImage = view.findViewById<MaterialCardView>(R.id.cvBikeImage)
        ivBikePreview = view.findViewById(R.id.ivBikePreview)

        val etCatID = view.findViewById<TextInputEditText>(R.id.etCatID)
        val etCatName = view.findViewById<TextInputEditText>(R.id.etCatName)
        val etBikeName = view.findViewById<TextInputEditText>(R.id.etBikeName)
        val etBrand = view.findViewById<TextInputEditText>(R.id.etBrand)
        val etColors = view.findViewById<TextInputEditText>(R.id.etColors)
        val etCC = view.findViewById<TextInputEditText>(R.id.etCC)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etStock = view.findViewById<TextInputEditText>(R.id.etStock)
        
        val etSpecEngine = view.findViewById<TextInputEditText>(R.id.etSpecEngine)
        val etSpecGear = view.findViewById<TextInputEditText>(R.id.etSpecGear)
        val etSpecBrake = view.findViewById<TextInputEditText>(R.id.etSpecBrake)
        val etSpecTorque = view.findViewById<TextInputEditText>(R.id.etSpecTorque)
        val etSpecMileage = view.findViewById<TextInputEditText>(R.id.etSpecMileage)
        val etSpecPower = view.findViewById<TextInputEditText>(R.id.etSpecPower)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnAddShopBike)

        cvBikeImage?.setOnClickListener { openGallery() }
        btnBack?.setOnClickListener { findNavController().popBackStack() }

        btnSave?.setOnClickListener {
            val catID = etCatID?.text?.toString()?.trim() ?: ""
            val catName = etCatName?.text?.toString()?.trim()?.lowercase() ?: ""
            val bikeName = etBikeName?.text?.toString()?.trim() ?: ""

            if (bikeName.isEmpty() || catID.isEmpty() || catName.isEmpty()) {
                Toast.makeText(context, "Hierarchy and Name are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val specs = BikeSpecs(
                brake = etSpecBrake?.text?.toString()?.trim() ?: "",
                engine = etSpecEngine?.text?.toString()?.trim() ?: "",
                gear = etSpecGear?.text?.toString()?.trim() ?: "",
                mileage = etSpecMileage?.text?.toString()?.trim() ?: "",
                power = etSpecPower?.text?.toString()?.trim() ?: "",
                torque = etSpecTorque?.text?.toString()?.trim() ?: ""
            )

            // Matching DB screenshot: colors as Map with numeric string keys
            val colorMap = etColors?.text?.toString()
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.withIndex()
                ?.associate { (it.index + 1).toString() to it.value }

            val bike = ShopBike(
                name = bikeName,
                brand = etBrand?.text?.toString()?.trim()?.uppercase() ?: "",
                cc = etCC?.text?.toString()?.trim()?.toIntOrNull(),
                colors = colorMap,
                img = localImagePath,
                price = etPrice?.text?.toString()?.trim()?.toLongOrNull(),
                stock = etStock?.text?.toString()?.trim()?.toIntOrNull(),
                specs = specs
            )

            saveBikeToStore(catID, catName, bike)
        }
    }

    private fun checkAdminAccess() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                val role = snapshot.value?.toString() ?: ""
                if (role != "admin") {
                    Toast.makeText(context, "Admin Access Only", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
    }

    private fun saveBikeToStore(catID: String, catName: String, bike: ShopBike) {
        val bikeRef = database.getReference("shop/bikes/categories")
            .child(catID).child(catName)
        
        val bikeKey = bikeRef.push().key ?: return
        bikeRef.child(bikeKey).setValue(bike).addOnCompleteListener {
            if (it.isSuccessful && isAdded) {
                Toast.makeText(context, "Bike Added Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveBikeImageLocally(uri: Uri): String {
        val folder = File(requireContext().filesDir, "bike_images")
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "bike_${System.currentTimeMillis()}.jpg")
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) { "" }
    }
}
