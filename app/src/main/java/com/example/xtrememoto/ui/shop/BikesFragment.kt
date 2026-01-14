package com.example.xtrememoto.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.adapter.ShopBikeAdapter
import com.example.xtrememoto.model.ShopBike
import com.example.xtrememoto.repository.ShopRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BikesFragment : Fragment(R.layout.fragment_bikes) {

    private lateinit var rvBikes: RecyclerView
    private lateinit var chipGroupCategories: ChipGroup
    private val repository = ShopRepository()
    private var allBikes = listOf<ShopBike>()
    private lateinit var adapter: ShopBikeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBikes = view.findViewById(R.id.rvBikes)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        
        rvBikes.layoutManager = GridLayoutManager(requireContext(), 2)
        
        adapter = ShopBikeAdapter { bike ->
            // ডিটেইল স্ক্রিনে যাওয়ার লজিক
        }
        rvBikes.adapter = adapter

        loadBikes()
        setupChipListeners()
    }

    private fun loadBikes() {
        repository.getAllShopBikes(
            onSuccess = { bikes ->
                allBikes = bikes
                filterBikes("All")
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupChipListeners() {
        chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()
                filterBikes(category)
            }
        }
    }

    private fun filterBikes(category: String) {
        val filteredList = if (category == "All") {
            allBikes
        } else {
            allBikes.filter { it.category?.equals(category, ignoreCase = true) == true }
        }
        adapter.submitList(filteredList)
        
        if (filteredList.isEmpty() && allBikes.isNotEmpty()) {
            Toast.makeText(requireContext(), "No bikes in this category", Toast.LENGTH_SHORT).show()
        }
    }
}