package com.adaptiveTrust.mobile.data.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.models.AlertMessage
import com.adaptiveTrust.mobile.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages alert push notifications for critical trust events.
 * Shows heads-up notifications when the app is in background.
 */
@Singleton
class AlertNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_CRITICAL = "trust_critical"
        const val CHANNEL_HIGH     = "trust_high"
        const val CHANNEL_INFO     = "trust_info"
    }

    init { createChannels() }

    private fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_CRITICAL,
            "Critical Trust Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Impossible travel, account suspension events"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_HIGH,
            "High Trust Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Failed logins, unknown device events" })

        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_INFO,
            "Trust Info",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Score changes, admin nudges" })
    }

    fun notifyAlert(alert: AlertMessage) {
        val channelId = when (alert.severity.uppercase()) {
            "CRITICAL" -> CHANNEL_CRITICAL
            "HIGH"     -> CHANNEL_HIGH
            else       -> CHANNEL_INFO
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "console")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠ ${alert.severity} Alert")
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(
                if (alert.severity == "CRITICAL") NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(alert.alertId.toInt(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silent fallback
        }
    }
}
