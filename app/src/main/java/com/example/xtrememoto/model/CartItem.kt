package com.example.xtrememoto.model

data class CartItem(
    val id: String? = null,
    val partId: String? = null,
    val partName: String? = null,
    val price: String? = null,
    val imageUrl: String? = null,
    val quantity: Int = 1,
    val userId: String? = null
)
