package com.adaptiveTrust.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.adaptiveTrust.mobile.data.api.AlertNotificationManager
import com.adaptiveTrust.mobile.data.api.WebSocketManager
import com.adaptiveTrust.mobile.data.local.TokenStore
import com.adaptiveTrust.mobile.data.repository.TrustRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic background worker that:
 * 1. Refreshes the user trust-score cache from the server every 5 minutes.
 * 2. Loads any historical alerts missed while the app was closed.
 *
 * The WebSocket for real-time alerts is managed separately by [WebSocketManager]
 * and is active only when the app is in the foreground.
 */
@HiltWorker
class TrustSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TrustRepository,
    private val tokenStore: TokenStore,
    private val notificationManager: AlertNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Skip if not authenticated
        if (!tokenStore.isLoggedInBlocking()) return Result.success()

        return try {
            // 1. Refresh user score cache
            repository.refreshUsers()

            // 2. Pull any alerts that arrived while WS was disconnected
            repository.loadHistoricalAlerts()

            Result.success()
        } catch (e: Exception) {
            // Retry on network failure (up to 3 times with backoff)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "trust_sync_periodic"

        /**
         * Enqueue a periodic sync that runs every 15 minutes (WorkManager minimum).
         * For the 5-second poll mentioned in the PDD, the WebSocket handles real-time
         * data — this worker handles background refresh when WS is unavailable.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<TrustSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /** One-shot immediate sync — call after login. */
        fun runOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<TrustSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
