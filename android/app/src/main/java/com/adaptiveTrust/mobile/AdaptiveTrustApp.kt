package com.adaptiveTrust.mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point with Hilt DI and manual WorkManager initialization
 * (required when using HiltWorkerFactory).
 */
@HiltAndroidApp
class AdaptiveTrustApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Notification channels are created lazily by AlertNotificationManager
        // via Hilt injection at first alert
    }
}
