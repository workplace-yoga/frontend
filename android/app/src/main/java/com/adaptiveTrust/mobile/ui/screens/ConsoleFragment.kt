package com.adaptiveTrust.mobile.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.api.WebSocketManager
import com.adaptiveTrust.mobile.data.local.TokenStore
import com.adaptiveTrust.mobile.data.models.AlertMessage
import com.adaptiveTrust.mobile.data.models.ConsoleEvent
import com.adaptiveTrust.mobile.viewmodel.AlertsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SCREEN 4: Real-time Console
 *
 * Split view:
 *   Top half  → Scrolling alert list (severity-colored)
 *   Bottom half → Decision console log (ANSI-style monospace stream)
 */
@AndroidEntryPoint
class ConsoleFragment : Fragment() {

    private val vm: AlertsViewModel by viewModels()

    @Inject lateinit var wsManager: WebSocketManager
    @Inject lateinit var tokenStore: TokenStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_console, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val llAlerts  = view.findViewById<LinearLayout>(R.id.ll_alerts)
        val llConsole = view.findViewById<LinearLayout>(R.id.ll_console)
        val scrollAlerts  = view.findViewById<ScrollView>(R.id.scroll_alerts)
        val scrollConsole = view.findViewById<ScrollView>(R.id.scroll_console)
        val btnClear  = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear_console)
        val tvAlertCount = view.findViewById<TextView>(R.id.tv_alert_count)

        btnClear.setOnClickListener {
            llConsole.removeAllViews()
            appendConsoleEntry(llConsole, scrollConsole, "── Console cleared ──", "#546E7A", "INFO")
        }

        // ── Observe alerts ─────────────────────────────────────────────────────
        vm.alerts.observe(viewLifecycleOwner) { alerts ->
            tvAlertCount.text = "${alerts.size} alerts"
            llAlerts.removeAllViews()
            alerts.take(50).forEach { cached ->
                val alert = AlertMessage(
                    alertId = cached.alertId,
                    userId = cached.userId,
                    severity = cached.severity,
                    message = cached.message,
                    ruleTriggered = cached.ruleTriggered,
                    createdAt = cached.createdAt
                )
                appendAlertRow(llAlerts, alert)
            }
        }

        // ── Observe console log ────────────────────────────────────────────────
        vm.consoleLog.observe(viewLifecycleOwner) { events ->
            llConsole.removeAllViews()
            events.take(100).reversed().forEach { event ->
                appendConsoleEntry(llConsole, scrollConsole, event.message, severityColor(event.severity), event.severity)
            }
        }

        // ── Wire WebSocket handlers ────────────────────────────────────────────
        wsManager.setAlertHandler { alert ->
            requireActivity().runOnUiThread {
                vm.ingestAlert(alert)
                vibrate()
            }
        }

        wsManager.setConsoleHandler { event ->
            requireActivity().runOnUiThread {
                vm.addConsoleEvent(event)
            }
        }

        // Connect WebSocket (token from DataStore)
        CoroutineScope(Dispatchers.IO).launch {
            val token = tokenStore.getTokenBlocking() ?: return@launch
            wsManager.connectAlerts(token)
            wsManager.connectConsole(token)
        }

        vm.loadHistorical()
    }

    private fun appendAlertRow(container: LinearLayout, alert: AlertMessage) {
        val tv = TextView(context).apply {
            val severityColor = if (alert.severity == "CRITICAL") "#FF1744" else "#FF9800"
            text = buildString {
                append("[${alert.createdAt.take(19).replace('T', ' ')}]  ")
                append("[${alert.severity}]  ")
                append(alert.message)
            }
            setTextColor(Color.parseColor(severityColor))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(16, 8, 16, 8)
            setBackgroundColor(Color.parseColor("#0D1117"))
        }
        container.addView(tv, 0)
    }

    private fun appendConsoleEntry(
        container: LinearLayout,
        scrollView: ScrollView,
        message: String,
        colorHex: String,
        severity: String
    ) {
        val prefix = when (severity) {
            "CRITICAL" -> "🔴"
            "WARN"     -> "🟡"
            else       -> "🟢"
        }
        val tv = TextView(context).apply {
            text = "$prefix  $message"
            setTextColor(Color.parseColor(colorHex))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(16, 4, 16, 4)
        }
        container.addView(tv)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun severityColor(severity: String) = when (severity) {
        "CRITICAL" -> "#FF1744"
        "WARN"     -> "#FFD600"
        else       -> "#00E676"
    }

    private fun vibrate() {
        val vibrator = requireContext().getSystemService(android.os.Vibrator::class.java)
        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't disconnect here – keep alive across tab switches
    }

    override fun onDestroy() {
        super.onDestroy()
        wsManager.disconnect()
    }
}
