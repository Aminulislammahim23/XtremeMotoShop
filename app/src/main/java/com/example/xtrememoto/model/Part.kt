package com.example.xtrememoto.model

data class Part(
    var id: String? = null,
    val brand: String? = null,
    val img: String? = null,
    val price: Long? = null,
    val stock: Int? = null,
    val type: String? = null,
    // Helper fields for DB path
    var categoryId: String? = null,
    var categoryName: String? = null
)
