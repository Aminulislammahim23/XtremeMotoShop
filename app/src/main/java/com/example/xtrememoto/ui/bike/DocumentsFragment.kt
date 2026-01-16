package com.example.xtrememoto.ui.bike

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.Bike
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DocumentsFragment : Fragment() {

    private lateinit var rvBikeDocuments: RecyclerView
    private lateinit var tvNoBike: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnAddUpdate: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val bikeList = mutableListOf<Bike>()
    private val selectedBikes = mutableListOf<Bike>()
    private lateinit var adapter: DocumentsAdapter
    private var isDeleteMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        rvBikeDocuments = view.findViewById(R.id.rvBikeDocuments)
        tvNoBike = view.findViewById(R.id.tvNoBike)
        btnDelete = view.findViewById(R.id.btnDeleteInfo)
        btnAddUpdate = view.findViewById(R.id.btnAddUpdateInfo)

        rvBikeDocuments.layoutManager = LinearLayoutManager(context)
        adapter = DocumentsAdapter(bikeList) { bike, isChecked ->
            if (isChecked) selectedBikes.add(bike) else selectedBikes.remove(bike)
        }
        rvBikeDocuments.adapter = adapter

        fetchBikeDocuments()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            if (isDeleteMode) {
                toggleDeleteMode(false)
            } else {
                findNavController().popBackStack()
            }
        }

        btnDelete.setOnClickListener {
            if (!isDeleteMode) {
                toggleDeleteMode(true)
            } else {
                showDeleteWarningDialog()
            }
        }
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        adapter.setDeleteMode(enabled)
        selectedBikes.clear()
        
        if (enabled) {
            btnDelete.text = "Confirm Delete"
            btnAddUpdate.text = "Cancel"
            btnAddUpdate.setOnClickListener { toggleDeleteMode(false) }
        } else {
            btnDelete.text = "Delete"
            btnAddUpdate.text = "Add/Update"
            btnAddUpdate.setOnClickListener { 
                // Navigate to Add/Update Screen logic
            }
        }
    }

    private fun showDeleteWarningDialog() {
        if (selectedBikes.isEmpty()) {
            Toast.makeText(context, "Please select at least one bike", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Information")
            .setMessage("Are you sure you want to delete the selected bike documents? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                confirmDeletion()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeletion() {
        val uid = auth.currentUser?.uid ?: return
        val bikesRef = database.getReference("users").child(uid).child("Bikes")
        
        selectedBikes.forEach { bike ->
            bike.id?.let { bikeId ->
                bikesRef.child(bikeId).removeValue()
            }
        }

        Toast.makeText(context, "Selected bikes deleted successfully", Toast.LENGTH_SHORT).show()
        toggleDeleteMode(false)
    }

    private fun fetchBikeDocuments() {
        val uid = auth.currentUser?.uid ?: return
        val bikesRef = database.getReference("users").child(uid).child("Bikes")

        bikesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bikeList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val bike = child.getValue(Bike::class.java)
                        bike?.let {
                            it.id = child.key
                            bikeList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    rvBikeDocuments.visibility = View.VISIBLE
                    tvNoBike.visibility = View.GONE
                } else {
                    rvBikeDocuments.visibility = View.GONE
                    tvNoBike.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class DocumentsAdapter(
    private val list: List<Bike>,
    private val onSelectChanged: (Bike, Boolean) -> Unit
) : RecyclerView.Adapter<DocumentsAdapter.ViewHolder>() {

    private var isDeleteMode = false

    fun setDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvModel: TextView = view.findViewById(R.id.tvBikeModel)
        val tvEngine: TextView = view.findViewById(R.id.tvEngineNum)
        val tvChassis: TextView = view.findViewById(R.id.tvChassisNum)
        val tvReg: TextView = view.findViewById(R.id.tvRegValue)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bike_document, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bike = list[position]
        holder.tvModel.text = bike.model ?: "Unknown Model"
        holder.tvEngine.text = "Engine No: ${bike.engNum ?: "N/A"}"
        holder.tvChassis.text = "Chassis No: ${bike.frameNum ?: "N/A"}"
        holder.tvReg.text = bike.reg ?: "Not Registered"

        holder.cbSelect.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = false 
        
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            onSelectChanged(bike, isChecked)
        }
    }

    override fun getItemCount() = list.size
}
