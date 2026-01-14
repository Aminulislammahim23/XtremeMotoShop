package com.example.xtrememoto.ui.service

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.xtrememoto.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddServiceBookingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var acTVselectDivision: AutoCompleteTextView
    private lateinit var acTVselectDistrict: AutoCompleteTextView
    private lateinit var database: FirebaseDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
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
        acTVselectDivision = view.findViewById(R.id.acTVselectDivision)
        acTVselectDistrict = view.findViewById(R.id.acTVselectDistrict)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fetchDivisions()
        fetchDistricts()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btnCurrentLocation).setOnClickListener {
            requestLocationPermission()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // ডিফল্ট ঢাকা সেট করে রাখছি
        val defaultLocation = LatLng(23.8103, 90.4125)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    private fun fetchDivisions() {
        val divisionRef = database.getReference("dealer/division")
        divisionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val divisions = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.value?.toString()?.let { divisions.add(it) }
                    }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        divisions
                    )
                    acTVselectDivision.setAdapter(adapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Division error: ${error.message}")
            }
        })
    }

    private fun fetchDistricts() {
        val districtRef = database.getReference("dealer/district")
        districtRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val districts = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.value?.toString()?.let { districts.add(it) }
                    }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        districts
                    )
                    acTVselectDistrict.setAdapter(adapter)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "District error: ${error.message}")
            }
        })
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.clear() // আগের মার্কার মুছে ফেলা
                googleMap?.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                Toast.makeText(context, "Location Found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Unable to find location. Is GPS on?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation()
        }
    }

    // MapView এর লাইফসাইকেল হ্যান্ডেল করা জরুরি
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}