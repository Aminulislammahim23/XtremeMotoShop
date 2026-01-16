package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ServiceBookingFragment : Fragment() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var llNoBooking: LinearLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ServiceBookingAdapter
    private val bookingList = mutableListOf<BookingModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        rvBookings = view.findViewById(R.id.rvBookings)
        llNoBooking = view.findViewById(R.id.llNoBooking)
        
        rvBookings.layoutManager = LinearLayoutManager(context)
        adapter = ServiceBookingAdapter(bookingList)
        rvBookings.adapter = adapter

        fetchBookings()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnAddBooking).setOnClickListener {
            findNavController().navigate(R.id.action_serviceBookingFragment_to_addServiceBookingFragment)
        }
    }

    private fun fetchBookings() {
        val uid = auth.currentUser?.uid ?: return
        val bookingRef = database.getReference("users").child(uid).child("service").child("booking")

        bookingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bookingList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val booking = child.getValue(BookingModel::class.java)
                        booking?.let { bookingList.add(it) }
                    }
                    bookingList.reverse() // লেটেস্ট বুকিং উপরে দেখানোর জন্য
                    adapter.notifyDataSetChanged()
                    
                    rvBookings.visibility = View.VISIBLE
                    llNoBooking.visibility = View.GONE
                } else {
                    rvBookings.visibility = View.GONE
                    llNoBooking.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ServiceBooking", "Error: ${error.message}")
            }
        })
    }
}

data class BookingModel(
    val dealer: String = "",
    val category: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Pending"
)

class ServiceBookingAdapter(private val list: List<BookingModel>) :
    RecyclerView.Adapter<ServiceBookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDealer: android.widget.TextView = view.findViewById(R.id.tvDealerName)
        val tvCategory: android.widget.TextView = view.findViewById(R.id.tvCategory)
        val tvDate: android.widget.TextView = view.findViewById(R.id.tvDate)
        val tvTime: android.widget.TextView = view.findViewById(R.id.tvTime)
        val tvStatus: android.widget.TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDealer.text = item.dealer
        holder.tvCategory.text = item.category
        holder.tvDate.text = item.date
        holder.tvTime.text = item.time
        holder.tvStatus.text = item.status
    }

    override fun getItemCount() = list.size
}
