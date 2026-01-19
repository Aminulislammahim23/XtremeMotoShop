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
    var id: String? = "",
    val name: String? = "",
    val brand: String? = "",
    val cc: Int? = null,
    val colors: Map<String, String>? = null,
    val img: String? = "",
    val price: Long? = null,
    val stock: Int? = null,
    val specs: BikeSpecs? = null,
    val category: String? = ""
)
