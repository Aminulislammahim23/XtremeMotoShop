package com.example.xtrememoto.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.adapter.ShopBikeAdapter
import com.example.xtrememoto.repository.ShopRepository

class BikesFragment : Fragment(R.layout.fragment_bikes) {

    private lateinit var rvBikes: RecyclerView
    private val repository = ShopRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // আইডিটি নিশ্চিত করুন আপনার XML এর সাথে মিলছে কি না (আমি rvBikes ব্যবহার করছি)
        rvBikes = view.findViewById(R.id.rvBikes)
        rvBikes.layoutManager = GridLayoutManager(requireContext(), 2)

        loadBikes()
    }

    private fun loadBikes() {
        repository.getAllShopBikes(
            onSuccess = { bikes ->
                if (bikes.isEmpty()) {
                    Toast.makeText(requireContext(), "No bikes found in database", Toast.LENGTH_SHORT).show()
                } else {
                    val adapter = ShopBikeAdapter { bike ->
                        // ডিটেইল স্ক্রিনে যাওয়ার লজিক
                    }
                    rvBikes.adapter = adapter
                    adapter.submitList(bikes)
                }
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }
}