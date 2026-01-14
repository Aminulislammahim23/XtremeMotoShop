package com.example.xtrememoto.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun signup(name: String, email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun saveUserToFirestore(uid: String, name: String, email: String): Task<Void> {
        val userData = hashMapOf(
            "fullName" to name,
            "email" to email
        )
        return db.collection("users").document(uid).set(userData)
    }

    fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun updateLastLogin(uid: String): Task<Void> {
        return database.child(uid).child("lastLogin").setValue(System.currentTimeMillis())
    }

    fun logout() {
        auth.signOut()
    }
}