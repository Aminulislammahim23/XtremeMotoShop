package com.example.xtrememoto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.xtrememoto.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    fun signup(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.signup(name, email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        // রিয়েলটাইম ডাটাবেসে ডাটা সেভ করা (আপনার আগের আর্কিটেকচার অনুযায়ী)
                        val database = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")
                        val userData = hashMapOf(
                            "Name" to name,
                            "Email" to email
                        )
                        database.child(uid).setValue(userData).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                _authState.value = AuthState.Success("Signup Successful")
                            } else {
                                _authState.value = AuthState.Error(dbTask.exception?.message ?: "Database error")
                            }
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Signup failed")
                }
            }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.login(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        repository.updateLastLogin(uid)
                            .addOnCompleteListener { dbTask ->
                                _authState.value = AuthState.Success("Login Successful")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun logout() {
        repository.logout()
    }

    sealed class AuthState {
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
