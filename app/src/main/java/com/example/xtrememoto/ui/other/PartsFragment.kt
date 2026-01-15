package com.example.xtrememoto.ui.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.adapter.PartAdapter
import com.example.xtrememoto.viewmodel.ShopViewModel

class PartsFragment : Fragment() {

    private val viewModel: ShopViewModel by viewModels()
    private lateinit var partAdapter: PartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parts, container, false)

        // RecyclerView Setup
        // যেহেতু আপনার fragment_parts.xml এ HorizontalScrollView আছে, 
        // আমি এখানে একটি ডাইনামিক অ্যাডাপ্টার সেটআপ করছি।
        
        val rvParts = view.findViewById<RecyclerView>(R.id.rvPartsList) ?: return view
        partAdapter = PartAdapter(emptyList())
        rvParts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvParts.adapter = partAdapter

        // ViewModel Observation
        viewModel.parts.observe(viewLifecycleOwner) { parts ->
            partAdapter.updateList(parts)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchAllParts()

        return view
    }
}
