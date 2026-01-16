package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.example.xtrememoto.adapter.ServiceHistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ServiceHistoryFragment : Fragment() {

    private lateinit var rvServiceHistory: RecyclerView
    private lateinit var tvNoHistory: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ServiceHistoryAdapter
    private val historyList = mutableListOf<BookingModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        rvServiceHistory = view.findViewById(R.id.rvServiceHistory)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)

        rvServiceHistory.layoutManager = LinearLayoutManager(context)
        
        // অ্যাডাপ্টারে ক্লিক লিসেনার পাস করা হচ্ছে
        adapter = ServiceHistoryAdapter(historyList) { booking ->
            showServiceDetailsDialog(booking)
        }
        rvServiceHistory.adapter = adapter

        fetchServiceHistory()
    }

    private fun showServiceDetailsDialog(booking: BookingModel) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_service_details, null)
        
        view.findViewById<TextView>(R.id.tvDialogDealer).text = booking.dealer
        view.findViewById<TextView>(R.id.tvDialogCategory).text = booking.category
        view.findViewById<TextView>(R.id.tvDialogDate).text = booking.date
        view.findViewById<TextView>(R.id.tvDialogTime).text = booking.time
        view.findViewById<TextView>(R.id.tvDialogStatus).text = booking.status
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun fetchServiceHistory() {
        val uid = auth.currentUser?.uid ?: return
        val historyRef = database.getReference("users").child(uid).child("service").child("history")

        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                historyList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val history = child.getValue(BookingModel::class.java)
                        history?.let { historyList.add(it) }
                    }
                    historyList.reverse()
                    adapter.notifyDataSetChanged()
                    
                    rvServiceHistory.visibility = View.VISIBLE
                    tvNoHistory.visibility = View.GONE
                } else {
                    rvServiceHistory.visibility = View.GONE
                    tvNoHistory.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ServiceHistory", "Error: ${error.message}")
            }
        })
    }
}
