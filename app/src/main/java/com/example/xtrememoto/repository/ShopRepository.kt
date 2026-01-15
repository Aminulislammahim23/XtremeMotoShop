package com.example.xtrememoto.repository

import android.util.Log
import com.example.xtrememoto.model.Part
import com.example.xtrememoto.model.ShopBike
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShopRepository {

    private val bikeRef = FirebaseDatabase.getInstance().getReference("shop/bikes/categories")
    private val partRef = FirebaseDatabase.getInstance().getReference("shop/parts/categories")

    fun getAllShopBikes(onSuccess: (List<ShopBike>) -> Unit, onError: (String) -> Unit) {
        bikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bikes = mutableListOf<ShopBike>()
                for (indexSnap in snapshot.children) {
                    for (categorySnap in indexSnap.children) {
                        val categoryName = categorySnap.key ?: ""
                        for (bikeSnap in categorySnap.children) {
                            val bike = ShopBike(
                                id = bikeSnap.key,
                                name = bikeSnap.child("name").value?.toString() ?: "No Name",
                                brand = bikeSnap.child("brand").value?.toString() ?: "No Brand",
                                cc = bikeSnap.child("cc").value?.toString() ?: "0",
                                price = bikeSnap.child("price").value?.toString() ?: "0",
                                stock = bikeSnap.child("stock").value?.toString() ?: "0",
                                img = bikeSnap.child("img").value?.toString(),
                                category = categoryName
                            )
                            bikes.add(bike)
                        }
                    }
                }
                onSuccess(bikes)
            }
            override fun onCancelled(error: DatabaseError) { onError(error.message) }
        })
    }

    fun getAllParts(onSuccess: (List<Part>) -> Unit, onError: (String) -> Unit) {
        partRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val parts = mutableListOf<Part>()
                if (!snapshot.exists()) {
                    onSuccess(emptyList())
                    return
                }
                for (indexSnap in snapshot.children) {
                    for (categorySnap in indexSnap.children) {
                        val categoryName = categorySnap.key ?: ""
                        for (partSnap in categorySnap.children) {
                            val part = Part(
                                id = partSnap.key,
                                brand = partSnap.child("brand").value?.toString(),
                                price = partSnap.child("price").value?.toString(),
                                stock = partSnap.child("stock").value?.toString(),
                                type = partSnap.child("type").value?.toString(),
                                category = categoryName
                            )
                            parts.add(part)
                        }
                    }
                }
                onSuccess(parts)
            }
            override fun onCancelled(error: DatabaseError) { onError(error.message) }
        })
    }
}
