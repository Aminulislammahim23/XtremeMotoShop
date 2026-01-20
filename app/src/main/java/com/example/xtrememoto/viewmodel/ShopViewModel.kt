package com.example.xtrememoto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.xtrememoto.model.CartItem
import com.example.xtrememoto.model.Part
import com.example.xtrememoto.model.ShopBike
import com.example.xtrememoto.repository.CartRepository
import com.example.xtrememoto.repository.ShopRepository

class ShopViewModel : ViewModel() {
    private val repository = ShopRepository()
    private val cartRepository = CartRepository()

    private val _bikes = MutableLiveData<List<ShopBike>>()
    val bikes: LiveData<List<ShopBike>> get() = _bikes

    private val _parts = MutableLiveData<List<Part>>()
    val parts: LiveData<List<Part>> get() = _parts

    private val _filteredParts = MutableLiveData<List<Part>>()
    val filteredParts: LiveData<List<Part>> get() = _filteredParts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _cartSuccess = MutableLiveData<Boolean>()
    val cartSuccess: LiveData<Boolean> get() = _cartSuccess

    fun fetchAllShopBikes() {
        repository.getAllShopBikes(
            onSuccess = { _bikes.postValue(it) },
            onError = { _error.postValue(it) }
        )
    }

    fun fetchAllParts() {
        repository.getAllParts(
            onSuccess = { 
                _parts.postValue(it)
                _filteredParts.postValue(it) 
            },
            onError = { _error.postValue(it) }
        )
    }

    fun filterPartsByCategory(category: String) {
        val currentParts = _parts.value ?: return
        if (category.equals("All", ignoreCase = true)) {
            _filteredParts.postValue(currentParts)
        } else {
            val filtered = currentParts.filter { 
                it.categoryName?.equals(category, ignoreCase = true) == true
            }
            _filteredParts.postValue(filtered)
        }
    }

    fun addToCart(part: Part) {
        val cartItem = CartItem(
            partId = part.id,
            partName = part.type,
            price = part.price?.toString(),
            imageUrl = part.img,
            quantity = 1
        )
        cartRepository.addToCart(cartItem, 
            onSuccess = { _cartSuccess.postValue(true) },
            onError = { _error.postValue(it) }
        )
    }
}
