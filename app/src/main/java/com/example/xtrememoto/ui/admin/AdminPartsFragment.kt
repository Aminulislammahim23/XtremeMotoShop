package com.example.xtrememoto.ui.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Part
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AdminPartsFragment : Fragment() {

    private lateinit var rvParts: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val partsList = mutableListOf<Part>()
    private lateinit var adapter: AdminPartsAdapter
    private var ivPartPreview: ImageView? = null
    private var selectedImageUri: Uri? = null

    private val categoryIds = mutableListOf<String>()
    private val categoryNames = mutableListOf<String>()
    private val idToNameMap = mutableMapOf<String, String>()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                selectedImageUri = it
                ivPartPreview?.setImageURI(it)
                ivPartPreview?.alpha = 1.0f
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_parts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvParts = view.findViewById(R.id.rvAdminParts)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddPart)
        val toolbar = view.findViewById<Toolbar>(R.id.adminPartsToolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rvParts.layoutManager = LinearLayoutManager(context)
        adapter = AdminPartsAdapter(partsList, 
            onDeleteClick = { part -> deletePart(part) },
            onViewClick = { part -> showPartDetailDialog(part) }
        )
        rvParts.adapter = adapter

        fetchPartsAndCategories()

        fabAdd.setOnClickListener {
            showAddPartDialog()
        }
    }

    private fun fetchPartsAndCategories() {
        val partsRef = database.getReference("shop/parts/categories")
        partsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                partsList.clear()
                val tempIds = mutableSetOf<String>()
                val tempNames = mutableSetOf<String>()
                idToNameMap.clear()

                for (catIdSnapshot in snapshot.children) {
                    val categoryId = catIdSnapshot.key ?: continue
                    tempIds.add(categoryId)
                    for (catNameSnapshot in catIdSnapshot.children) {
                        val categoryName = catNameSnapshot.key ?: continue
                        tempNames.add(categoryName)
                        idToNameMap[categoryId] = categoryName
                        
                        for (partSnapshot in catNameSnapshot.children) {
                            val brand = partSnapshot.child("brand").value?.toString()
                            val img = partSnapshot.child("img").value?.toString()
                            val price = partSnapshot.child("price").value?.toString()?.toLongOrNull()
                            val stock = partSnapshot.child("stock").value?.toString()?.toIntOrNull()
                            val type = partSnapshot.child("type").value?.toString()

                            val part = Part(
                                id = partSnapshot.key,
                                brand = brand,
                                img = img,
                                price = price,
                                stock = stock,
                                type = type,
                                categoryId = categoryId,
                                categoryName = categoryName
                            )
                            partsList.add(part)
                        }
                    }
                }
                categoryIds.clear()
                categoryIds.addAll(tempIds)
                categoryNames.clear()
                categoryNames.addAll(tempNames)
                
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showPartDetailDialog(part: Part) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_part_detail, null)
        val ivImage = dialogView.findViewById<ImageView>(R.id.ivDetailPartImage)
        val tvName = dialogView.findViewById<TextView>(R.id.tvDetailPartName)
        val tvBrand = dialogView.findViewById<TextView>(R.id.tvDetailPartBrand)
        val tvCategory = dialogView.findViewById<TextView>(R.id.tvDetailPartCategory)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tvDetailPartPrice)
        val tvStock = dialogView.findViewById<TextView>(R.id.tvDetailPartStock)
        val tvType = dialogView.findViewById<TextView>(R.id.tvDetailPartType)

        tvName.text = part.type ?: "No Type Specified"
        tvBrand.text = "Brand: ${part.brand ?: "Unknown"}"
        tvCategory.text = "Category: ${part.categoryName ?: "N/A"}"
        tvPrice.text = "৳ ${part.price ?: 0}"
        tvStock.text = "${part.stock ?: 0} units"
        tvType.text = "Type: ${part.type ?: "N/A"}"

        if (!part.img.isNullOrEmpty()) {
            val file = File(part.img!!)
            if (file.exists()) {
                ivImage.setImageURI(Uri.fromFile(file))
            } else {
                ivImage.setImageResource(R.drawable.ic_parts)
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAddPartDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_part, null)
        val etCatId = dialogView.findViewById<AutoCompleteTextView>(R.id.etCatID)
        val etCatName = dialogView.findViewById<AutoCompleteTextView>(R.id.etCatName)
        val etType = dialogView.findViewById<EditText>(R.id.etType)
        val etBrand = dialogView.findViewById<EditText>(R.id.etBrand)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val etStock = dialogView.findViewById<EditText>(R.id.etStock)
        val cvPartImage = dialogView.findViewById<MaterialCardView>(R.id.cvPartImage)
        ivPartPreview = dialogView.findViewById(R.id.ivPartPreview)

        val idAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryIds)
        etCatId.setAdapter(idAdapter)

        val nameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        etCatName.setAdapter(nameAdapter)

        etCatId.setOnItemClickListener { parent, _, position, _ ->
            val selectedId = parent.getItemAtPosition(position).toString()
            val name = idToNameMap[selectedId]
            if (name != null) {
                etCatName.setText(name)
            }
        }

        selectedImageUri = null

        cvPartImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Part")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val catId = etCatId.text.toString().trim()
                val catName = etCatName.text.toString().trim().lowercase()
                val type = etType.text.toString().trim()
                val brand = etBrand.text.toString().trim()
                val price = etPrice.text.toString().toLongOrNull() ?: 0L
                val stock = etStock.text.toString().toIntOrNull() ?: 0

                if (catId.isNotEmpty() && catName.isNotEmpty() && type.isNotEmpty()) {
                    var localPath = ""
                    selectedImageUri?.let {
                        localPath = saveImageLocally(it)
                    }

                    val part = Part(
                        brand = brand,
                        type = type,
                        price = price,
                        stock = stock,
                        img = localPath
                    )
                    savePartToDb(catId, catName, part)
                } else {
                    Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveImageLocally(uri: Uri): String {
        val folder = File(requireContext().filesDir, "part_images")
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "part_${System.currentTimeMillis()}.jpg")
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) { "" }
    }

    private fun savePartToDb(catId: String, catName: String, part: Part) {
        val ref = database.getReference("shop/parts/categories").child(catId).child(catName)
        val key = ref.push().key ?: return
        ref.child(key).setValue(part).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Part Added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePart(part: Part) {
        if (part.categoryId != null && part.categoryName != null && part.id != null) {
            database.getReference("shop/parts/categories")
                .child(part.categoryId!!)
                .child(part.categoryName!!)
                .child(part.id!!)
                .removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Part Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

class AdminPartsAdapter(
    private val list: List<Part>,
    private val onDeleteClick: (Part) -> Unit,
    private val onViewClick: (Part) -> Unit
) : RecyclerView.Adapter<AdminPartsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOfferTitle)
        val tvSubtitle: TextView? = view.findViewById(R.id.tvOfferDesc)
        val btnView: Button = view.findViewById(R.id.btnViewPost)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val part = list[position]
        holder.tvTitle.text = "${part.type} (${part.brand})"
        holder.tvSubtitle?.text = "Price: ${part.price} | Stock: ${part.stock} | Cat: ${part.categoryName}"
        holder.btnView.setOnClickListener { onViewClick(part) }
        holder.btnDelete.setOnClickListener { onDeleteClick(part) }
    }

    override fun getItemCount() = list.size
}
