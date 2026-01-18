package com.example.xtrememoto.ui.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*

class DealerFragment : Fragment() {

    private lateinit var rvDealers: RecyclerView
    private lateinit var database: FirebaseDatabase
    private val dealerList = mutableListOf<String>()
    private lateinit var adapter: DealerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dealer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvDealers = view.findViewById(R.id.rvDealers)
        
        rvDealers.layoutManager = LinearLayoutManager(context)
        adapter = DealerAdapter(dealerList) { dealerName ->
            showDealerDetails(dealerName)
        }
        rvDealers.adapter = adapter

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        fetchAllDealers()
    }

    private fun showDealerDetails(dealerName: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_dealer_details, null)
        
        view.findViewById<TextView>(R.id.tvDetailDealerName).text = dealerName
        view.findViewById<TextView>(R.id.tvDetailDealerType).text = "XtremeMoto Dealer"
        
        // কল করার লজিক (স্যাম্পল নম্বর ব্যবহার করা হয়েছে)
        view.findViewById<Button>(R.id.btnCall).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:01700000000") // এখানে ডিলারের নম্বর বসবে
            startActivity(intent)
        }

        // WhatsApp-এ মেসেজ পাঠানোর লজিক
        view.findViewById<Button>(R.id.btnWhatsapp).setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=+8801700000000" // এখানে ডিলারের নম্বর বসবে
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun fetchAllDealers() {
        val dealerRef = database.getReference("dealer/division")
        dealerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                dealerList.clear()
                for (divisionChild in snapshot.children) {
                    val districtsNode = divisionChild.child("district")
                    for (districtChild in districtsNode.children) {
                        for (dealerChild in districtChild.children) {
                            dealerChild.key?.let { dealerList.add(it) }
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DealerFragment", "Error: ${error.message}")
            }
        })
    }
}

class DealerAdapter(
    private val list: List<String>,
    private val onViewClick: (String) -> Unit
) : RecyclerView.Adapter<DealerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDealerName: TextView = view.findViewById(R.id.tvDealerName)
        val tvDealerType: TextView = view.findViewById(R.id.tvDealerType)
        val ivDealerImage: ImageView = view.findViewById(R.id.ivDealerImage)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dealer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dealerName = list[position]
        holder.tvDealerName.text = dealerName
        holder.tvDealerType.text = "XtremeMoto Dealer"
        holder.ivDealerImage.setImageResource(R.drawable.ic_logo) 
        
        holder.btnView.setOnClickListener {
            onViewClick(dealerName)
        }
    }

    override fun getItemCount() = list.size
}
