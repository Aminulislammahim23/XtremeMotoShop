package com.example.xtrememoto.repository

import android.util.Log
import com.example.xtrememoto.model.BikeSpecs
import com.example.xtrememoto.model.ShopBike
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShopRepository {

    private val dbRef = FirebaseDatabase.getInstance()
        .getReference("shop/bikes/categories/1")

    fun getAllShopBikes(
        onSuccess: (List<ShopBike>) -> Unit,
        onError: (String) -> Unit
    ) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bikes = mutableListOf<ShopBike>()
                
                if (!snapshot.exists()) {
                    onSuccess(emptyList())
                    return
                }

                for (categorySnap in snapshot.children) {
                    val categoryName = categorySnap.key ?: ""
                    
                    for (bikeSnap in categorySnap.children) {
                        try {
                            // সরাসরি .value.toString() ব্যবহার করা হচ্ছে যাতে null না আসে
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
                        } catch (e: Exception) {
                            Log.e("ShopRepo", "Error: ${e.message}")
                        }
                    }
                }
                onSuccess(bikes)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }
}