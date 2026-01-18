package com.example.xtrememoto.ui.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xtrememoto.R
import com.google.firebase.database.*

class OffersFragment : Fragment() {

    private lateinit var rvOffers: RecyclerView
    private lateinit var tvNoOffers: TextView
    private lateinit var database: FirebaseDatabase
    private val offerList = mutableListOf<OfferModel>()
    private lateinit var adapter: OffersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        rvOffers = view.findViewById(R.id.rvOffers)
        tvNoOffers = view.findViewById(R.id.tvNoOffers)

        rvOffers.layoutManager = LinearLayoutManager(context)
        adapter = OffersAdapter(offerList) {
            // বুক নাও ক্লিক করলে সার্ভিস বুকিং পেজে নিয়ে যাবে
            findNavController().navigate(R.id.action_homeFragment_to_serviceBookingFragment)
        }
        rvOffers.adapter = adapter

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        fetchOffersFromDB()
    }

    private fun fetchOffersFromDB() {
        // আপনার লেটেস্ট স্ক্রিনশট অনুযায়ী রুট নোড 'offers' থেকে ডাটা আনা হচ্ছে
        val offerRef = database.getReference("offers")
        offerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                offerList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val offer = child.getValue(OfferModel::class.java)
                        offer?.let { offerList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                    rvOffers.visibility = View.VISIBLE
                    tvNoOffers.visibility = View.GONE
                } else {
                    rvOffers.visibility = View.GONE
                    tvNoOffers.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OffersFragment", "Database Error: ${error.message}")
            }
        })
    }
}

data class OfferModel(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val buttonText: String = "Book Now",
    val expiryDate: String = ""
)

class OffersAdapter(
    private val list: List<OfferModel>,
    private val onBookClick: () -> Unit
) : RecyclerView.Adapter<OffersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOfferTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvOfferDesc)
        val ivBanner: ImageView = view.findViewById(R.id.ivOfferBanner)
        val btnBook: Button = view.findViewById(R.id.btnBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = list[position]
        holder.tvTitle.text = offer.title
        
        // ডেসক্রিপশন এবং এক্সপায়ারি ডেট একসাথে দেখানো হচ্ছে
        val fullDesc = if (offer.expiryDate.isNotEmpty()) 
            "${offer.description}\nValid till: ${offer.expiryDate}" 
            else offer.description
        holder.tvDesc.text = fullDesc
        
        holder.btnBook.text = offer.buttonText

        // ডিফল্ট ইমেজ লোড
        holder.ivBanner.setImageResource(R.drawable.banner_offers)
        
        holder.btnBook.setOnClickListener { onBookClick() }
    }

    override fun getItemCount() = list.size
}
