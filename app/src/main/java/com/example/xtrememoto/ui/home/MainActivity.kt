package com.example.xtrememoto.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.xtrememoto.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (item.itemId == R.id.homeFragment && currentUser != null) {
                val uid = currentUser.uid
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                
                userRef.child("role").get().addOnSuccessListener { snapshot ->
                    val role = snapshot.value?.toString() ?: "customer"
                    if (role == "admin") {
                        navController.navigate(R.id.adminFragment)
                    } else {
                        navController.navigate(R.id.homeFragment)
                    }
                }.addOnFailureListener {
                    navController.navigate(R.id.homeFragment)
                }
                true
            } else {
                // Default behavior for other items
                val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                handled
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.onboardingFragment1,
                R.id.onboardingFragment2,
                R.id.onboardingFragment3,
                R.id.signupFragment,
                R.id.loginFragment,
                R.id.adminFragment,
                R.id.adminOffersFragment,
                R.id.adminBikesFragment,
                R.id.adminPartsFragment,
                R.id.adminBookingFragment,
                R.id.adminProfileFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}
