package com.example.xtrememoto.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvServiceDays: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvBikeName = view.findViewById<TextView>(R.id.tvBikeName)
        tvServiceDays = view.findViewById(R.id.tvServiceDays)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        val user = auth.currentUser

        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(uid)
            
            // নাম এবং প্রোফাইল পিকচার পাথ লোড
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && isAdded) {
                        val name = snapshot.child("Name").value?.toString() ?: ""
                        val profilePicPath = snapshot.child("profilePic").value?.toString() ?: ""
                        
                        tvUserName.text = name

                        if (profilePicPath.isNotEmpty()) {
                            val imgFile = File(profilePicPath)
                            if (imgFile.exists()) {
                                val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                                ivProfileImage.setImageBitmap(myBitmap)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // সিলেক্টেড বাইকের নাম নিয়ে আসা
            userRef.child("selectedBike").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value?.toString() ?: ""
                        val model = snapshot.child("model").value?.toString() ?: ""
                        tvBikeName.text = "$name $model".uppercase()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // লেটেস্ট বুকিং থেকে অবশিষ্ট দিন ক্যালকুলেট করা
            fetchLatestBookingAndCalculateDays(uid)
        }

        view.findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        view.findViewById<Button>(R.id.btnChange).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myBikeFragment)
        }

        setupNavigation(view)
        setupExpandCollapse(view)
    }

    private fun fetchLatestBookingAndCalculateDays(uid: String) {
        val bookingRef = database.getReference("users").child(uid).child("service").child("booking")
        
        bookingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                if (snapshot.exists()) {
                    var latestBookingDateStr = ""
                    var maxId = -1L

                    for (child in snapshot.children) {
                        val id = child.key?.toLongOrNull() ?: -1L
                        if (id >= maxId) {
                            maxId = id
                            latestBookingDateStr = child.child("date").value?.toString() ?: ""
                        }
                    }

                    if (latestBookingDateStr.isNotEmpty()) {
                        calculateRemainingDays(latestBookingDateStr)
                    } else {
                        tvServiceDays.text = "No active booking"
                    }
                } else {
                    tvServiceDays.text = "No service booked"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateRemainingDays(bookingDateStr: String) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        try {
            val bookingDate = sdf.parse(bookingDateStr)
            if (bookingDate != null) {
                val diffInMillis = bookingDate.time - today.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                tvServiceDays.text = when {
                    diffInDays == 0L -> "Service Due Today!"
                    diffInDays == 1L -> "Due Tomorrow"
                    diffInDays > 1 -> "In $diffInDays days"
                    diffInDays < 0 -> "Overdue by ${-diffInDays} days"
                    else -> "Date: $bookingDateStr"
                }
            }
        } catch (e: Exception) {
            tvServiceDays.text = "Checkup due: $bookingDateStr"
        }
    }

    private fun setupNavigation(view: View) {
        view.findViewById<MaterialCardView>(R.id.cvBikeDocs).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_documentsFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServiceHistory).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceHistoryFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServicePackages).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_servicePackageFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServiceBooking).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceBookingFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvParts).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_partsFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvBikes).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bikesFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvDealers).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_dealerFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvServiceSchedule).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_serviceScheduleFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cvWarrantyPolicy).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_warrantyFragment)
        }
        // cvOffers ক্লিক করলে সরাসরি OffersFragment-এ নিয়ে যাবে
        view.findViewById<MaterialCardView>(R.id.cvOffers).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_offersFragment)
        }
    }

    private fun setupExpandCollapse(view: View) {
        val ivExpandCollapse = view.findViewById<ImageView>(R.id.buttonExp)
        val expandableLayout = view.findViewById<LinearLayout>(R.id.expandable_layout)

        ivExpandCollapse.setOnClickListener {
            if (expandableLayout.visibility == View.VISIBLE) {
                expandableLayout.visibility = View.GONE
                ivExpandCollapse.setImageResource(R.drawable.ic_expand_more)
            } else {
                expandableLayout.visibility = View.VISIBLE
                ivExpandCollapse.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }
}
