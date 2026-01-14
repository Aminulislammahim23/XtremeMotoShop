package com.example.xtrememoto.model

data class BikeSpecs(
    val brake: String? = "",
    val engine: String? = "",
    val gear: String? = "",
    val mileage: String? = "",
    val power: String? = "",
    val torque: String? = ""
)

data class ShopBike(
    val id: String? = "",
    val name: String? = "",
    val brand: String? = "",
    val cc: String? = "",
    val colors: List<String>? = null, // এখন এটি লিস্ট
    val img: String? = "",
    val price: String? = "",
    val stock: String? = "",
    val specs: BikeSpecs? = null,
    val category: String? = ""
)
