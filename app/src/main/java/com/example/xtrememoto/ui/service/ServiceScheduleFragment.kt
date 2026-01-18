package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ServiceScheduleFragment : Fragment() {

    private lateinit var rvSchedule: RecyclerView
    private lateinit var tvBikeName: TextView
    private lateinit var tvPurchaseDate: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val scheduleList = mutableListOf<ScheduleItem>()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        tvBikeName = view.findViewById(R.id.tvBikeName)
        tvPurchaseDate = view.findViewById(R.id.tvPurchaseDate)
        rvSchedule = view.findViewById(R.id.rvSchedule)

        rvSchedule.layoutManager = LinearLayoutManager(context)
        adapter = ScheduleAdapter(scheduleList)
        rvSchedule.adapter = adapter

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        fetchSelectedBikeHeader()
        fetchBookingsAndCalculateDays()
    }

    private fun fetchSelectedBikeHeader() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("selectedBike")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value?.toString() ?: ""
                        val model = snapshot.child("model").value?.toString() ?: ""
                        tvBikeName.text = "$name $model".uppercase()
                        tvPurchaseDate.text = "Active Service Plan"
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchBookingsAndCalculateDays() {
        val uid = auth.currentUser?.uid ?: return
        val bookingRef = database.getReference("users").child(uid).child("service").child("booking")

        bookingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                scheduleList.clear()
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val today = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                var index = 1
                for (child in snapshot.children) {
                    val bookingDateStr = child.child("date").value?.toString() ?: ""
                    val category = child.child("category").value?.toString() ?: "Service"
                    val dealer = child.child("dealer").value?.toString() ?: ""

                    var dueText = "Due: $bookingDateStr"
                    
                    if (bookingDateStr.isNotEmpty()) {
                        try {
                            val bookingDate = sdf.parse(bookingDateStr)
                            if (bookingDate != null) {
                                val diffInMillis = bookingDate.time - today.time
                                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                                dueText = when {
                                    diffInDays == 0L -> "Due Today"
                                    diffInDays == 1L -> "Due Tomorrow"
                                    diffInDays > 1 -> "Due in $diffInDays days ($bookingDateStr)"
                                    diffInDays < 0 -> "Overdue by ${-diffInDays} days"
                                    else -> "Due: $bookingDateStr"
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Schedule", "Parse error: ${e.message}")
                        }
                    }

                    val ordinal = when (index) {
                        1 -> "1st"
                        2 -> "2nd"
                        3 -> "3rd"
                        else -> "${index}th"
                    }

                    scheduleList.add(ScheduleItem(ordinal, category, dueText, dealer))
                    index++
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

data class ScheduleItem(
    val index: String,
    val title: String,
    val date: String,
    val kms: String
)

class ScheduleAdapter(private val list: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tvMonthIndex)
        val tvTitle: TextView = view.findViewById(R.id.tvScheduleTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDueDate)
        val tvKms: TextView = view.findViewById(R.id.tvKms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvIndex.text = item.index
        holder.tvTitle.text = item.title
        holder.tvDate.text = item.date
        holder.tvKms.text = item.kms
    }

    override fun getItemCount() = list.size
}
