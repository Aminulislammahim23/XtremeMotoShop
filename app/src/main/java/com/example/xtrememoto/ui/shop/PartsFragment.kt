package com.example.xtrememoto.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.adapter.PartAdapter
import com.example.xtrememoto.viewmodel.ShopViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PartsFragment : Fragment() {

    private val viewModel: ShopViewModel by viewModels()
    private lateinit var partAdapter: PartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parts, container, false)

        val rvParts = view.findViewById<RecyclerView>(R.id.rvPartsList)
        val cgCategories = view.findViewById<ChipGroup>(R.id.cgCategories)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvNoData = view.findViewById<TextView>(R.id.tvNoData)

        // RecyclerView Setup
        partAdapter = PartAdapter(emptyList())
        rvParts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvParts.adapter = partAdapter

        // ViewModel Observation
        viewModel.filteredParts.observe(viewLifecycleOwner) { parts ->
            if (parts.isNullOrEmpty()) {
                rvParts.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
            } else {
                rvParts.visibility = View.VISIBLE
                tvNoData.visibility = View.GONE
                partAdapter.updateList(parts)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }

        // Chip selection logic
        cgCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()
                viewModel.filterPartsByCategory(category)
            }
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Initial Data Fetch
        viewModel.fetchAllParts()

        return view
    }
}
