package com.example.xtrememoto.repository

import com.example.xtrememoto.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val cartRef = database.getReference("cart")

    fun addToCart(cartItem: CartItem, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("User not logged in")
        val itemId = cartItem.partId ?: return onError("Invalid Part ID")
        
        val rootRef = database.reference
        val cartPath = "cart/$userId/$itemId"
        val userCartPath = "users/$userId/cart/$itemId"

        rootRef.child(cartPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = hashMapOf<String, Any?>()
                
                if (snapshot.exists()) {
                    val currentQty = snapshot.child("quantity").getValue(Int::class.java) ?: 1
                    val newQty = currentQty + 1
                    
                    updates["$cartPath/quantity"] = newQty
                    updates["$userCartPath/quantity"] = newQty
                } else {
                    val finalItem = cartItem.copy(userId = userId)
                    
                    updates[cartPath] = finalItem
                    updates[userCartPath] = finalItem
                }

                rootRef.updateChildren(updates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Failed to sync cart data") }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    fun getCartItems(onSuccess: (List<CartItem>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onSuccess(emptyList())
        cartRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                onSuccess(items)
            }
            override fun onCancelled(error: DatabaseError) { onError(error.message) }
        })
    }
}
