package com.example.xtrememoto.ui.service

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AddServiceBookingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var acTVselectDivision: AutoCompleteTextView
    private lateinit var acTVselectDistrict: AutoCompleteTextView
    private lateinit var acTVselectCategory: AutoCompleteTextView
    private lateinit var acTVselectDealer: AutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var etNote: TextInputEditText
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var llBookingDetails: LinearLayout
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_service_booking, container, false)
        mapView = view.findViewById(R.id.mvNearestDealer)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        
        acTVselectDivision = view.findViewById(R.id.acTVselectDivision)
        acTVselectDistrict = view.findViewById(R.id.acTVselectDistrict)
        acTVselectCategory = view.findViewById(R.id.acTVselectCategory)
        acTVselectDealer = view.findViewById(R.id.acTVselectDealer)
        etDate = view.findViewById(R.id.etDate)
        etTime = view.findViewById(R.id.etTime)
        etNote = view.findViewById(R.id.etNote)
        llBookingDetails = view.findViewById(R.id.llBookingDetails)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fetchDivisions()
        fetchCategories()

        setupListeners(view)
    }

    private fun setupListeners(view: View) {
        acTVselectDivision.setOnItemClickListener { parent, _, position, _ ->
            val selectedDivision = parent.getItemAtPosition(position).toString()
            acTVselectDistrict.setText("", false)
            acTVselectDealer.setText("", false)
            fetchDistricts(selectedDivision)
        }

        acTVselectDistrict.setOnItemClickListener { parent, _, position, _ ->
            val selectedDistrict = parent.getItemAtPosition(position).toString()
            val selectedDivision = acTVselectDivision.text.toString()
            acTVselectDealer.setText("", false)
            fetchDealers(selectedDivision, selectedDistrict)
        }

        acTVselectCategory.setOnClickListener {
            acTVselectCategory.showDropDown()
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btnCurrentLocation).setOnClickListener {
            requestLocationPermission()
        }

        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val division = acTVselectDivision.text.toString()
            val district = acTVselectDistrict.text.toString()

            if (division.isNotEmpty() && district.isNotEmpty()) {
                llBookingDetails.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Please select Division and District", Toast.LENGTH_SHORT).show()
            }
        }

        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }

        view.findViewById<Button>(R.id.btnBook).setOnClickListener {
            saveBookingData()
        }
    }

    private fun saveBookingData() {
        val uid = auth.currentUser?.uid ?: return
        val division = acTVselectDivision.text.toString()
        val district = acTVselectDistrict.text.toString()
        val dealer = acTVselectDealer.text.toString()
        val category = acTVselectCategory.text.toString()
        val date = etDate.text.toString()
        val time = etTime.text.toString()
        val note = etNote.text.toString()

        if (dealer.isEmpty() || category.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // service নোডের রেফারেন্স নেওয়া হচ্ছে (যেখানে booking এবং history উভয়ই আছে)
        val serviceRef = database.getReference("users").child(uid).child("service")
        
        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // booking এবং history নোডের বর্তমান সংখ্যা অনুযায়ী পরবর্তী নিউমেরিক আইডি জেনারেট করা
                val bookingCount = snapshot.child("booking").childrenCount
                val historyCount = snapshot.child("history").childrenCount
                
                val nextBookingId = (bookingCount + 1).toString()
                val nextHistoryId = (historyCount + 1).toString()

                val bookingData = hashMapOf(
                    "division" to division,
                    "district" to district,
                    "dealer" to dealer,
                    "category" to category,
                    "date" to date,
                    "time" to time,
                    "note" to note,
                    "status" to "Pending",
                    "timestamp" to System.currentTimeMillis()
                )

                // মাল্টিপল পাথে ডাটা সেভ করার জন্য updates ম্যাপ তৈরি
                val updates = hashMapOf<String, Any>(
                    "booking/$nextBookingId" to bookingData,
                    "history/$nextHistoryId" to bookingData
                )

                // updateChildren ব্যবহার করে একসাথেই booking এবং history-তে ডাটা সেভ করা হচ্ছে
                serviceRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful && isAdded) {
                        Toast.makeText(context, "Booking successful!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(context, "Booking failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddServiceBooking", "Database Error: ${error.message}")
            }
        })
    }

    private fun fetchCategories() {
        val categoryRef = database.getReference("service/categories")
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val categories = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.key?.let { categories.add(it) }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
                    acTVselectCategory.threshold = 0
                    acTVselectCategory.setAdapter(adapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchDivisions() {
        val divisionRef = database.reference.child("dealer").child("division")
        divisionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val divisions = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.key?.let { divisions.add(it) }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, divisions)
                    acTVselectDivision.setAdapter(adapter)
                    fetchUserLocationPreference()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchUserLocationPreference() {
        val uid = auth.currentUser?.uid ?: return
        database.reference.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val userDivision = snapshot.child("Division").value?.toString()
                val userDivisionFixed = when(userDivision) {
                    "Chittagong" -> "Chattogram"
                    else -> userDivision
                }
                val userDistrict = snapshot.child("District").value?.toString()

                if (!userDivisionFixed.isNullOrEmpty()) {
                    acTVselectDivision.setText(userDivisionFixed, false)
                    fetchDistricts(userDivisionFixed, userDistrict)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchDistricts(division: String, prefillDistrict: String? = null) {
        val districtRef = database.reference.child("dealer").child("division").child(division).child("district")
        districtRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val districts = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.key?.let { districts.add(it) }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, districts)
                    acTVselectDistrict.setAdapter(adapter)
                    
                    if (!prefillDistrict.isNullOrEmpty() && districts.contains(prefillDistrict)) {
                        acTVselectDistrict.setText(prefillDistrict, false)
                        fetchDealers(division, prefillDistrict)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchDealers(division: String, district: String) {
        val dealerRef = database.reference.child("dealer").child("division").child(division).child("district").child(district)
        dealerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val dealers = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.key?.let { dealers.add(it) }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dealers)
                    acTVselectDealer.setAdapter(adapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Date").build()
        datePicker.show(childFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            etDate.setText(sdf.format(Date(selection)))
        }
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(12).setMinute(0).setTitleText("Select Time").build()
        timePicker.show(childFragmentManager, "TIME_PICKER")
        timePicker.addOnPositiveButtonClickListener {
            val hour = if (timePicker.hour > 12) timePicker.hour - 12 else if (timePicker.hour == 0) 12 else timePicker.hour
            val amPm = if (timePicker.hour >= 12) "PM" else "AM"
            etTime.setText(String.format("%02d:%02d %s", hour, timePicker.minute, amPm))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
        val defaultLocation = LatLng(23.8103, 90.4125)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        googleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateMapLocation(location.latitude, location.longitude)
            } else {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
                fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { updateMapLocation(it.latitude, it.longitude) }
                    }
                }, Looper.getMainLooper())
            }
        }
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val currentLatLng = LatLng(lat, lng)
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        llBookingDetails.visibility = View.VISIBLE
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
