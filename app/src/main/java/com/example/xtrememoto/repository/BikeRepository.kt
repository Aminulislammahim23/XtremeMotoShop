package com.example.xtrememoto.repository

import com.example.xtrememoto.model.Bike
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BikeRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun getUserName(uid: String, onResult: (String?) -> Unit) {
        database.getReference("users").child(uid).child("Name").get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.value?.toString())
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addBike(bike: Bike, onComplete: (Boolean, String?) -> Unit) {
        val userId = currentUserId ?: return
        val bikesRef = database.getReference("users").child(userId).child("Bikes")

        bikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nextIndex = (snapshot.childrenCount + 1).toString()
                bikesRef.child(nextIndex).setValue(bike)
                    .addOnSuccessListener {
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.message)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    fun getBikes(onResult: (List<Bike>) -> Unit) {
        val userId = currentUserId ?: return
        database.getReference("users").child(userId).child("Bikes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bikes = mutableListOf<Bike>()
                    for (bikeSnapshot in snapshot.children) {
                        val bike = bikeSnapshot.getValue(Bike::class.java)
                        bike?.let { bikes.add(it) }
                    }
                    onResult(bikes)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }
}