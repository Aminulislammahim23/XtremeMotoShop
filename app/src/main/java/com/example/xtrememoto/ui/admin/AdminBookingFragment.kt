package com.example.xtrememoto.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.google.firebase.database.*

class AdminBookingFragment : Fragment() {

    private lateinit var rvAdminBookings: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var database: FirebaseDatabase
    private val bookingList = mutableListOf<BookingAdminModel>()
    private lateinit var adapter: AdminBookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvAdminBookings = view.findViewById(R.id.rvAdminBookings)
        progressBar = view.findViewById(R.id.progressBar)

        // Back button listener
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        rvAdminBookings.layoutManager = LinearLayoutManager(context)
        adapter = AdminBookingAdapter(bookingList) { booking, newStatus ->
            updateBookingStatus(booking, newStatus)
        }
        rvAdminBookings.adapter = adapter

        fetchAllBookings()
    }

    private fun fetchAllBookings() {
        progressBar.visibility = View.VISIBLE
        val usersRef = database.getReference("users")
        
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bookingList.clear()
                
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key ?: continue
                        val userEmail = userSnapshot.child("Email").value?.toString() 
                            ?: userSnapshot.child("email").value?.toString() 
                            ?: "No Email"
                        
                        val serviceBookingRef = userSnapshot.child("service").child("booking")
                        
                        if (serviceBookingRef.exists()) {
                            for (bookingSnapshot in serviceBookingRef.children) {
                                val bookingId = bookingSnapshot.key ?: continue
                                val bikeName = bookingSnapshot.child("bikeName").value?.toString() ?: "Unknown Bike"
                                val dealer = bookingSnapshot.child("dealer").value?.toString() ?: ""
                                val date = bookingSnapshot.child("date").value?.toString() ?: ""
                                val time = bookingSnapshot.child("time").value?.toString() ?: ""
                                val status = bookingSnapshot.child("status").value?.toString() ?: "Pending"
                                
                                bookingList.add(BookingAdminModel(userId, userEmail, bookingId, bikeName, dealer, date, time, status))
                            }
                        }
                    }
                }
                
                bookingList.reverse() 
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun updateBookingStatus(booking: BookingAdminModel, newStatus: String) {
        val userRef = database.getReference("users").child(booking.userId).child("service")
        
        userRef.child("booking").child(booking.bookingId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                updateHistoryStatus(booking.userId, booking.bookingId, newStatus)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateHistoryStatus(userId: String, bookingId: String, newStatus: String) {
        val historyRef = database.getReference("users").child(userId).child("service").child("history")
        historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    if (child.key == bookingId) {
                        child.ref.child("status").setValue(newStatus)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

data class BookingAdminModel(
    val userId: String,
    val userEmail: String,
    val bookingId: String,
    val bikeName: String,
    val dealer: String,
    val date: String,
    val time: String,
    val status: String
)

class AdminBookingAdapter(
    private val list: List<BookingAdminModel>,
    private val onActionClick: (BookingAdminModel, String) -> Unit
) : RecyclerView.Adapter<AdminBookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvBikeName: TextView = view.findViewById(R.id.tvBikeName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDealer: TextView = view.findViewById(R.id.tvDealer)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = list[position]
        holder.tvUserEmail.text = booking.userEmail
        holder.tvBikeName.text = booking.bikeName
        holder.tvStatus.text = booking.status
        holder.tvDealer.text = booking.dealer
        holder.tvDate.text = booking.date
        holder.tvTime.text = booking.time

        when (booking.status) {
            "Completed" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                holder.btnComplete.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
            "Cancelled" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                holder.btnComplete.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.btnComplete.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.VISIBLE
            }
        }

        holder.btnCancel.setOnClickListener { onActionClick(booking, "Cancelled") }
        holder.btnComplete.setOnClickListener { onActionClick(booking, "Completed") }
    }

    override fun getItemCount() = list.size
}
