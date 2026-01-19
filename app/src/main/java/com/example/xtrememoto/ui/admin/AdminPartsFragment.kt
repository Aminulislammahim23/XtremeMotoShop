package com.example.xtrememoto.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Part // Assuming you have a Part model
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AdminPartsFragment : Fragment() {

    private lateinit var rvParts: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val partsList = mutableListOf<Any>() // Use your actual Part model
    private val partsKeys = mutableListOf<String>()
    private lateinit var adapter: AdminPartsAdapter

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
        adapter = AdminPartsAdapter(partsList) { key ->
            deletePart(key)
        }
        rvParts.adapter = adapter

        fetchParts()

        fabAdd.setOnClickListener {
            Toast.makeText(context, "Add Part Dialog Placeholder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchParts() {
        val partsRef = database.getReference("shop/parts")
        partsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                partsList.clear()
                partsKeys.clear()
                for (child in snapshot.children) {
                    // map to your model
                    child.child("name").value?.let {
                        partsList.add(it)
                        partsKeys.add(child.key ?: "")
                    }
                }
                adapter.setKeys(partsKeys)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun deletePart(key: String) {
        database.getReference("shop/parts").child(key).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Part Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class AdminPartsAdapter(
    private val list: List<Any>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminPartsAdapter.ViewHolder>() {

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
        holder.tvTitle.text = list[position].toString()
        holder.btnDelete.setOnClickListener { onDeleteClick(keys[position]) }
    }

    override fun getItemCount() = list.size
}
