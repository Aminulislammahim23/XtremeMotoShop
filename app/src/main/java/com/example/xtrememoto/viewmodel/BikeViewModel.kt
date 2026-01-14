package com.example.xtrememoto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.xtrememoto.model.Bike
import com.example.xtrememoto.repository.BikeRepository

class BikeViewModel : ViewModel() {

    private val repository = BikeRepository()

    private val _bikeList = MutableLiveData<List<Bike>>()
    val bikeList: LiveData<List<Bike>> get() = _bikeList

    private val _addBikeStatus = MutableLiveData<BikeStatus>()
    val addBikeStatus: LiveData<BikeStatus> get() = _addBikeStatus

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    fun fetchUserName() {
        val uid = repository.currentUserId
        if (uid != null) {
            repository.getUserName(uid) { name ->
                _userName.value = name
            }
        }
    }

    fun addBike(bike: Bike) {
        _addBikeStatus.value = BikeStatus.Loading
        repository.addBike(bike) { success, error ->
            if (success) {
                _addBikeStatus.value = BikeStatus.Success("Bike Added Successfully!")
            } else {
                _addBikeStatus.value = BikeStatus.Error(error ?: "Failed to add bike")
            }
        }
    }

    fun fetchBikes() {
        repository.getBikes { bikes ->
            _bikeList.value = bikes
        }
    }

    sealed class BikeStatus {
        object Loading : BikeStatus()
        data class Success(val message: String) : BikeStatus()
        data class Error(val message: String) : BikeStatus()
    }
}