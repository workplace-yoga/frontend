package com.adaptiveTrust.mobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.local.TokenStore
import com.adaptiveTrust.mobile.worker.TrustSyncWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main host activity – Industrial Command Center dark theme.
 * Houses bottom navigation with 3 tabs:
 *   1. Glanceable Feed (Dashboard)
 *   2. Real-time Console
 *   3. Sensitivity Slider
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var tokenStore: TokenStore

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Guard: redirect to login if not authenticated
        if (!tokenStore.isLoggedInBlocking()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Ensure background sync is running
        TrustSyncWorker.enqueue(applicationContext)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Override back stack for bottom nav tabs
        bottomNav.setOnItemReselectedListener { /* No-op: don't pop back stack on reselect */ }

        // Handle deep-link from alert notification
        intent?.getStringExtra("navigate_to")?.let { dest ->
            when (dest) {
                "console" -> navController.navigate(R.id.consoleFragment)
                "feed"    -> navController.navigate(R.id.dashboardFragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
