package com.example.xtrememoto.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.model.BikeSpecs
import com.example.xtrememoto.model.ShopBike
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AdminBikesFragment : Fragment() {

    private lateinit var rvBikes: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val bikeList = mutableListOf<ShopBike>()
    private val bikePaths = mutableListOf<String>()
    private lateinit var adapter: AdminBikesAdapter

    private val categoryIds = mutableListOf<String>()
    private val categoryNames = mutableListOf<String>()
    private val idToNameMap = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            inflater.inflate(R.layout.fragment_admin_bikes, container, false)
        } catch (e: Exception) {
            Log.e("AdminBikes", "Error inflating layout: ${e.message}", e)
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            database = FirebaseDatabase.getInstance()
            rvBikes = view.findViewById(R.id.rvAdminBikes)
            val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddBike)
            val toolbar = view.findViewById<Toolbar>(R.id.adminBikesToolbar)

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            rvBikes.layoutManager = LinearLayoutManager(context)
            adapter = AdminBikesAdapter(bikeList,
                onDeleteClick = { path -> deleteBike(path) },
                onViewClick = { position -> 
                    val bike = bikeList[position]
                    val path = bikePaths[position]
                    showBikeDialog(bike, path) 
                }
            )
            rvBikes.adapter = adapter

            fetchBikesAndCategories()

            fabAdd.setOnClickListener {
                showBikeDialog()
            }
        } catch (e: Exception) {
            Log.e("AdminBikes", "Error in onViewCreated: ${e.message}", e)
            if (isAdded) Toast.makeText(context, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchBikesAndCategories() {
        try {
            val bikeRef = database.getReference("shop/bikes/categories")
            bikeRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    try {
                        bikeList.clear()
                        bikePaths.clear()
                        val tempIds = mutableSetOf<String>()
                        val tempNames = mutableSetOf<String>()
                        idToNameMap.clear()

                        for (catIdChild in snapshot.children) {
                            val catId = catIdChild.key ?: continue
                            tempIds.add(catId)
                            for (catNameChild in catIdChild.children) {
                                val catName = catNameChild.key ?: continue
                                tempNames.add(catName)
                                idToNameMap[catId] = catName

                                for (bikeChild in catNameChild.children) {
                                    val bike = bikeChild.getValue(ShopBike::class.java)
                                    bike?.let {
                                        it.id = bikeChild.key
                                        bikeList.add(it)
                                        bikePaths.add("shop/bikes/categories/$catId/$catName/${bikeChild.key}")
                                    }
                                }
                            }
                        }
                        categoryIds.clear()
                        categoryIds.addAll(tempIds)
                        categoryNames.clear()
                        categoryNames.addAll(tempNames)

                        adapter.setPaths(bikePaths)
                        adapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.e("AdminBikes", "Error processing data: ${e.message}", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdminBikes", "Database Error: ${error.message}")
                    if (isAdded) Toast.makeText(context, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("AdminBikes", "Error fetching bikes: ${e.message}", e)
        }
    }

    private fun showBikeDialog(existingBike: ShopBike? = null, path: String? = null) {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bike, null)
            val etCatId = dialogView.findViewById<AutoCompleteTextView>(R.id.etBikeCatID)
            val etCatName = dialogView.findViewById<AutoCompleteTextView>(R.id.etBikeCatName)
            val etName = dialogView.findViewById<EditText>(R.id.etBikeName)
            val etBrand = dialogView.findViewById<EditText>(R.id.etBikeBrand)
            val etCC = dialogView.findViewById<EditText>(R.id.etBikeCC)
            val etPrice = dialogView.findViewById<EditText>(R.id.etBikePrice)
            val etStock = dialogView.findViewById<EditText>(R.id.etBikeStock)
            val etColors = dialogView.findViewById<EditText>(R.id.etBikeColors)

            val etBrake = dialogView.findViewById<EditText>(R.id.etSpecBrake)
            val etEngine = dialogView.findViewById<EditText>(R.id.etSpecEngine)
            val etGear = dialogView.findViewById<EditText>(R.id.etSpecGear)
            val etMileage = dialogView.findViewById<EditText>(R.id.etSpecMileage)
            val etPower = dialogView.findViewById<EditText>(R.id.etSpecPower)
            val etTorque = dialogView.findViewById<EditText>(R.id.etSpecTorque)

            if (etCatId != null && etCatName != null) {
                val idAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryIds)
                etCatId.setAdapter(idAdapter)

                val nameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
                etCatName.setAdapter(nameAdapter)

                etCatId.setOnItemClickListener { parent, _, position, _ ->
                    val id = parent.getItemAtPosition(position).toString()
                    idToNameMap[id]?.let { etCatName.setText(it) }
                }
            }

            // Pre-fill fields if editing
            existingBike?.let { bike ->
                etName.setText(bike.name)
                etBrand.setText(bike.brand)
                etCC.setText(bike.cc?.toString())
                etPrice.setText(bike.price?.toString())
                etStock.setText(bike.stock?.toString())
                etColors.setText(bike.colors?.values?.joinToString(", "))
                
                bike.specs?.let { specs ->
                    etBrake.setText(specs.brake)
                    etEngine.setText(specs.engine)
                    etGear.setText(specs.gear)
                    etMileage.setText(specs.mileage)
                    etPower.setText(specs.power)
                    etTorque.setText(specs.torque)
                }

                // Try to extract catId and catName from path
                path?.let { p ->
                    val parts = p.split("/")
                    if (parts.size >= 5) {
                        etCatId.setText(parts[3])
                        etCatName.setText(parts[4])
                    }
                }
            }

            val dialogTitle = if (existingBike == null) "Add New Bike" else "Update Bike"
            val buttonText = if (existingBike == null) "Add" else "Update"

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setPositiveButton(buttonText, null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    val catId = etCatId?.text?.toString()?.trim() ?: ""
                    val catName = etCatName?.text?.toString()?.trim()?.lowercase() ?: ""
                    val bikeName = etName?.text?.toString()?.trim() ?: ""

                    if (catId.isEmpty() || catName.isEmpty() || bikeName.isEmpty()) {
                        Toast.makeText(context, "Category ID, Name and Bike Name are required", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val colorsList = etColors?.text?.toString()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                    val colorsMap = colorsList.withIndex().associate { (it.index + 1).toString() to it.value }

                    val specs = BikeSpecs(
                        brake = etBrake?.text?.toString() ?: "",
                        engine = etEngine?.text?.toString() ?: "",
                        gear = etGear?.text?.toString() ?: "",
                        mileage = etMileage?.text?.toString() ?: "",
                        power = etPower?.text?.toString() ?: "",
                        torque = etTorque?.text?.toString() ?: ""
                    )

                    val bike = ShopBike(
                        name = bikeName,
                        brand = etBrand?.text?.toString()?.trim()?.uppercase() ?: "",
                        cc = etCC?.text?.toString()?.toIntOrNull() ?: 0,
                        price = etPrice?.text?.toString()?.toLongOrNull() ?: 0L,
                        stock = etStock?.text?.toString()?.toIntOrNull() ?: 0,
                        colors = colorsMap,
                        specs = specs,
                        img = existingBike?.img ?: ""
                    )

                    if (existingBike == null) {
                        saveBikeToDb(catId, catName, bike)
                    } else {
                        updateBikeInDb(path!!, catId, catName, bike)
                    }
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e("AdminBikes", "Error in Add/Update button: ${e.message}", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AdminBikes", "Error showing dialog: ${e.message}", e)
            if (isAdded) Toast.makeText(context, "Critical Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateBikeInDb(oldPath: String, newCatId: String, newCatName: String, bike: ShopBike) {
        try {
            val newPath = "shop/bikes/categories/$newCatId/$newCatName/${oldPath.substringAfterLast("/")}"
            
            if (oldPath == newPath) {
                database.getReference(oldPath).setValue(bike).addOnCompleteListener {
                    if (it.isSuccessful && isAdded) Toast.makeText(context, "Bike Updated", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If category changed, move the data
                database.getReference(oldPath).removeValue()
                database.getReference(newPath).setValue(bike).addOnCompleteListener {
                    if (it.isSuccessful && isAdded) Toast.makeText(context, "Bike Updated & Moved", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AdminBikes", "Exception in updateBikeInDb: ${e.message}", e)
        }
    }

    private fun saveBikeToDb(catId: String, catName: String, bike: ShopBike) {
        try {
            val ref = database.getReference("shop/bikes/categories").child(catId).child(catName)
            val key = ref.push().key ?: return
            ref.child(key).setValue(bike).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (isAdded) Toast.makeText(context, "Bike Added", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("AdminBikes", "Failed to save: ${it.exception?.message}")
                    if (isAdded) Toast.makeText(context, "Save Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AdminBikes", "Exception in saveBikeToDb: ${e.message}", e)
        }
    }

    private fun deleteBike(path: String) {
        try {
            database.getReference(path).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (isAdded) Toast.makeText(context, "Bike Removed", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("AdminBikes", "Delete failed: ${it.exception?.message}")
                    if (isAdded) Toast.makeText(context, "Delete Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AdminBikes", "Exception in deleteBike: ${e.message}", e)
        }
    }
}

class AdminBikesAdapter(
    private val list: List<ShopBike>,
    private val onDeleteClick: (String) -> Unit,
    private val onViewClick: (Int) -> Unit
) : RecyclerView.Adapter<AdminBikesAdapter.ViewHolder>() {

    private var paths = listOf<String>()

    fun setPaths(newPaths: List<String>) {
        paths = newPaths
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOfferTitle)
        val btnView: Button = view.findViewById(R.id.btnViewPost)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val bike = list[position]
            holder.tvTitle.text = "${bike.name} (${bike.brand})"
            holder.btnView.setOnClickListener { onViewClick(position) }
            holder.btnDelete.setOnClickListener { onDeleteClick(paths[position]) }
        } catch (e: Exception) {
            Log.e("AdminBikesAdapter", "Error binding view holder: ${e.message}", e)
        }
    }

    override fun getItemCount() = list.size
}
