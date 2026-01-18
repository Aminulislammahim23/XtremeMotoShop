package com.example.xtrememoto.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.ui.service.OfferModel
import com.example.xtrememoto.ui.service.OffersAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AdminOffersFragment : Fragment() {

    private lateinit var rvOffers: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val offerList = mutableListOf<OfferModel>()
    private val offerKeys = mutableListOf<String>()
    private lateinit var adapter: AdminOffersAdapter

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

        rvOffers.layoutManager = LinearLayoutManager(context)
        adapter = AdminOffersAdapter(offerList) { key ->
            deleteOffer(key)
        }
        rvOffers.adapter = adapter

        fetchOffers()

        fabAdd.setOnClickListener {
            showAddOfferDialog()
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

    private fun showAddOfferDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_offer, null)

        val etTitle = view.findViewById<EditText>(R.id.etOfferTitle)
        val etDesc = view.findViewById<EditText>(R.id.etOfferDesc)
        val etExpiry = view.findViewById<EditText>(R.id.etOfferExpiry)
        val etButtonText = view.findViewById<EditText>(R.id.etOfferButtonText)
        val btnSave = view.findViewById<Button>(R.id.btnSaveOffer)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val btnText = etButtonText.text.toString().trim()

            if (title.isNotEmpty() && desc.isNotEmpty()) {
                val newOffer = OfferModel(title, desc, "", if(btnText.isEmpty()) "Book Now" else btnText, expiry)
                database.getReference("offers").push().setValue(newOffer).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Offer Added", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.setContentView(view)
        dialog.show()
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
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminOffersAdapter.ViewHolder>() {

    private var keys = listOf<String>()

    fun setKeys(newKeys: List<String>) {
        keys = newKeys
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: android.widget.TextView = view.findViewById(R.id.tvOfferTitle)
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = list[position].title
        holder.btnDelete.setOnClickListener { onDeleteClick(keys[position]) }
    }

    override fun getItemCount() = list.size
}
