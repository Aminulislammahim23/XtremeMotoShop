package com.example.xtrememoto.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.example.xtrememoto.ui.service.OfferModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AdminOffersFragment : Fragment() {

    private lateinit var rvOffers: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val offerList = mutableListOf<OfferModel>()
    private val offerKeys = mutableListOf<String>()
    private lateinit var adapter: AdminOffersAdapter
    
    private var selectedImageUri: Uri? = null
    private lateinit var ivOfferPreview: ImageView
    private var localImagePath: String = ""

    // ইমেজ পিকার লঞ্চার
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                selectedImageUri = it
                ivOfferPreview.setImageURI(it)
                ivOfferPreview.alpha = 1.0f // ইমেজ পুরোপুরি দেখাবে
                // লোকাল স্টোরেজে ছবি সেভ করা
                localImagePath = saveOfferImageToInternal(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvOffers = view.findViewById(R.id.rvAdminOffers)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddOffer)
        val toolbar = view.findViewById<Toolbar>(R.id.adminToolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rvOffers.layoutManager = LinearLayoutManager(context)
        adapter = AdminOffersAdapter(
            list = offerList,
            onDeleteClick = { key -> deleteOffer(key) },
            onViewClick = { offer, key -> showOfferDetailsDialog(offer, key) }
        )
        rvOffers.adapter = adapter

        fetchOffers()

        fabAdd.setOnClickListener {
            showOfferDetailsDialog(null, null)
        }
    }

    private fun fetchOffers() {
        val offerRef = database.getReference("offers")
        offerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                offerList.clear()
                offerKeys.clear()
                for (child in snapshot.children) {
                    val offer = child.getValue(OfferModel::class.java)
                    offer?.let {
                        offerList.add(it)
                        offerKeys.add(child.key ?: "")
                    }
                }
                adapter.setKeys(offerKeys)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showOfferDetailsDialog(offer: OfferModel?, key: String?) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_offer, null)

        val tvTitleHeader = view.findViewById<TextView>(R.id.tvAddOfferTitle)
        val etTitle = view.findViewById<EditText>(R.id.etOfferTitle)
        val etDesc = view.findViewById<EditText>(R.id.etOfferDesc)
        val cvSelectImage = view.findViewById<MaterialCardView>(R.id.cvOfferImage)
        ivOfferPreview = view.findViewById(R.id.ivOfferBannerPreview)
        val etExpiry = view.findViewById<EditText>(R.id.etOfferExpiry)
        val etButtonText = view.findViewById<EditText>(R.id.etOfferButtonText)
        val btnSave = view.findViewById<Button>(R.id.btnSaveOffer)

        if (offer != null) {
            tvTitleHeader.text = "Offer Details"
            etTitle.setText(offer.title)
            etDesc.setText(offer.description)
            etExpiry.setText(offer.expiryDate)
            etButtonText.setText(offer.buttonText)
            btnSave.text = "Update Offer"
            
            if (offer.imageUrl.isNotEmpty()) {
                val imgFile = File(offer.imageUrl)
                if (imgFile.exists()) {
                    ivOfferPreview.setImageURI(Uri.fromFile(imgFile))
                    ivOfferPreview.alpha = 1.0f
                }
            }
            localImagePath = offer.imageUrl
        }

        cvSelectImage.setOnClickListener {
            openGallery()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val btnText = etButtonText.text.toString().trim()

            if (title.isNotEmpty() && desc.isNotEmpty()) {
                val offerData = OfferModel(
                    title, 
                    desc, 
                    localImagePath, 
                    if(btnText.isEmpty()) "Book Now" else btnText, 
                    expiry
                )
                
                val dbRef = if (key == null) {
                    database.getReference("offers").push()
                } else {
                    database.getReference("offers").child(key)
                }

                dbRef.setValue(offerData).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val msg = if (key == null) "Offer Added Successfully" else "Offer Updated Successfully"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        localImagePath = ""
                        dialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(context, "Title and Description are required", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveOfferImageToInternal(uri: Uri): String {
        val folder = File(requireContext().filesDir, "offer_images")
        if (!folder.exists()) folder.mkdirs()

        val fileName = "offer_${System.currentTimeMillis()}.jpg"
        val file = File(folder, fileName)

        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    private fun deleteOffer(key: String) {
        database.getReference("offers").child(key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Offer Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class AdminOffersAdapter(
    private val list: List<OfferModel>,
    private val onDeleteClick: (String) -> Unit,
    private val onViewClick: (OfferModel, String) -> Unit
) : RecyclerView.Adapter<AdminOffersAdapter.ViewHolder>() {

    private var keys = listOf<String>()

    fun setKeys(newKeys: List<String>) {
        keys = newKeys
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: android.widget.TextView = view.findViewById(R.id.tvOfferTitle)
        val btnViewPost: android.widget.Button = view.findViewById(R.id.btnViewPost)
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = list[position]
        val key = keys[position]
        
        holder.tvTitle.text = offer.title
        holder.btnViewPost.setOnClickListener { onViewClick(offer, key) }
        holder.btnDelete.setOnClickListener { onDeleteClick(key) }
    }

    override fun getItemCount() = list.size
}
