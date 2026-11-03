package com.adaptiveTrust.mobile.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.models.RemoveUserRequest
import com.adaptiveTrust.mobile.ui.BiometricHelper
import com.adaptiveTrust.mobile.viewmodel.UiState
import com.adaptiveTrust.mobile.viewmodel.UserDetailViewModel
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

/**
 * SCREEN 2: Nudge Panel
 *
 * Full user detail with:
 *   - Trust score radar chart (S, Wa, Wh visualization)
 *   - [+Verify +20] [+Approve +15] [-Suspend -100] buttons
 *   - Nudge history log
 *   - All high-risk actions gated by BiometricPrompt
 */
@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private val vm: UserDetailViewModel by viewModels()
    private val args: UserDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_user_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUsername   = view.findViewById<TextView>(R.id.tv_detail_username)
        val tvScore      = view.findViewById<TextView>(R.id.tv_detail_score)
        val tvStatus     = view.findViewById<TextView>(R.id.tv_detail_status)
        val tvDept       = view.findViewById<TextView>(R.id.tv_detail_dept)
        val tvEmail      = view.findViewById<TextView>(R.id.tv_detail_email)
        val tvLocation   = view.findViewById<TextView>(R.id.tv_detail_location)
        val tvIp         = view.findViewById<TextView>(R.id.tv_detail_ip)
        val tvWa         = view.findViewById<TextView>(R.id.tv_wa_score)
        val tvWh         = view.findViewById<TextView>(R.id.tv_wh_score)
        val tvFlags      = view.findViewById<TextView>(R.id.tv_flags)
        val radarChart   = view.findViewById<RadarChart>(R.id.radar_chart)
        val btnVerify    = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_verify)
        val btnApprove   = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_approve)
        val btnSuspend   = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_suspend)
        val btnRemove    = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_remove)
        val progressBar  = view.findViewById<ProgressBar>(R.id.progress_nudge)
        val tvNudgeResult= view.findViewById<TextView>(R.id.tv_nudge_result)
        val llHistory    = view.findViewById<LinearLayout>(R.id.ll_nudge_history)
        val tvLastUpdated= view.findViewById<TextView>(R.id.tv_last_updated)

        val userId = args.userId

        // ── Nudge button click handler ─────────────────────────────────────────
        fun nudgeWithBiometric(action: String, label: String) {
            if (!BiometricHelper.isAvailable(requireActivity())) {
                Toast.makeText(context, "Biometric not available on this device", Toast.LENGTH_SHORT).show()
                return
            }
            BiometricHelper.authenticate(
                activity = requireActivity(),
                title = "Authorise $label",
                subtitle = "Touch the fingerprint sensor to confirm",
                description = "This high-risk action requires biometric verification.",
                onSuccess = { token ->
                    vm.applyNudge(userId, action, null, token)
                },
                onFailure = { err ->
                    tvNudgeResult.text = "❌ $err"
                    tvNudgeResult.setTextColor(Color.parseColor("#FF1744"))
                    tvNudgeResult.visibility = View.VISIBLE
                }
            )
        }

        btnVerify.setOnClickListener  { nudgeWithBiometric("Verify",  "+Verify") }
        btnApprove.setOnClickListener { nudgeWithBiometric("Approve", "+Approve") }
        btnSuspend.setOnClickListener { nudgeWithBiometric("Suspend", "−Suspend") }

        btnRemove.setOnClickListener {
            if (!BiometricHelper.isAvailable(requireActivity())) {
                Toast.makeText(context, "Biometric not available on this device", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            BiometricHelper.authenticate(
                activity = requireActivity(),
                title = "Authorise User Removal",
                subtitle = "Confirm permanent removal",
                description = "This requires biometric verification.",
                onSuccess = { token ->
                    vm.removeUser(userId, RemoveUserRequest(token),
                        onSuccess = {
                            Toast.makeText(context, "User removed successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        },
                        onError = { err ->
                            tvNudgeResult.text = "❌ $err"
                            tvNudgeResult.setTextColor(Color.parseColor("#FF1744"))
                            tvNudgeResult.visibility = View.VISIBLE
                        }
                    )
                },
                onFailure = { err ->
                    tvNudgeResult.text = "❌ $err"
                    tvNudgeResult.setTextColor(Color.parseColor("#FF1744"))
                    tvNudgeResult.visibility = View.VISIBLE
                }
            )
        }

        val tvEmployeeId = view.findViewById<TextView>(R.id.tv_employee_id)
        val dossierAccent = view.findViewById<View>(R.id.dossier_accent)

        // ── Observe user detail ────────────────────────────────────────────────
        vm.userState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> progressBar.visibility = View.VISIBLE
                is UiState.Error   -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    val u = state.data
                    val color = Color.parseColor(u.statusBadge.colorHex)

                    tvUsername.text = u.username.uppercase()
                    tvScore.text    = "${u.finalScore}"
                    tvScore.setTextColor(color)
                    tvStatus.text   = u.status.uppercase()
                    tvStatus.setTextColor(color)
                    dossierAccent.setBackgroundColor(color)
                    
                    tvDept.text     = u.department?.uppercase() ?: "GENERAL_SECTOR"
                    tvEmail.text    = u.email?.uppercase() ?: "NO_IDENTITY_LINK"
                    tvEmployeeId.text = "SEC-${userId.takeLast(4).uppercase()}-XJ"
                    
                    tvLocation.text = "📍 LAST_LOC: " + listOfNotNull(u.lastLocationCity, u.lastLocationCountry).joinToString(", ").uppercase()
                    tvIp.text       = "🌐 IP: " + (u.lastIp ?: "—")
                    tvWa.text       = "Wa (Auto): ${u.baseScore}"
                    tvWh.text       = "Wh (Human): ${if (u.humanAdjustment >= 0) "+${u.humanAdjustment}" else "${u.humanAdjustment}"}"
                    tvLastUpdated.text = "UPDATED: ${u.lastUpdated.take(19).replace('T', ' ')}"

                    // Flags
                    val flagParts = buildList {
                        if (u.flags.impossibleTravel) add("✈ IMPOSSIBLE_TRAVEL")
                        if (u.flags.failedLogins)     add("🔑 BRUTE_FORCE_DETECTED")
                        if (u.flags.unknownDevice)    add("📱 ROGUE_DEVICE")
                    }
                    tvFlags.text = "⚠️ ACTIVE_FLAGS: " + (if (flagParts.isEmpty()) "NONE" else flagParts.joinToString(" • "))

                    // Radar chart – visualize score components
                    setupRadarChart(radarChart, u.baseScore.toFloat(), u.humanAdjustment.toFloat(), u.finalScore.toFloat(), color)


                    // Nudge history
                    llHistory.removeAllViews()
                    u.nudgeHistory.take(10).forEach { n ->
                        val delta = if (n.whDelta >= 0) "+${n.whDelta}" else "${n.whDelta}"
                        val tv = TextView(context).apply {
                            text = "  ${n.appliedAt.take(10)}  [${n.action}]  Wh${delta}  by ${n.adminUsername}  ${if (n.biometricVerified) "🔒" else "⚠️"}"
                            setTextColor(Color.parseColor("#B0BEC5"))
                            textSize = 12f
                            setPadding(0, 6, 0, 6)
                        }
                        llHistory.addView(tv)
                    }
                }
            }
        }

        // ── Observe nudge result ───────────────────────────────────────────────
        vm.nudgeResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvNudgeResult.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    val r = state.data
                    tvNudgeResult.text = "✅ ${r.message}"
                    tvNudgeResult.setTextColor(Color.parseColor(r.statusBadge.colorHex))
                    tvNudgeResult.visibility = View.VISIBLE
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvNudgeResult.text = "❌ ${state.message}"
                    tvNudgeResult.setTextColor(Color.parseColor("#FF1744"))
                    tvNudgeResult.visibility = View.VISIBLE
                }
            }
        }

        vm.loadUser(userId)
    }

    private fun setupRadarChart(chart: RadarChart, wa: Float, wh: Float, score: Float, color: Int) {
        val labels = listOf("Auto (Wa)", "Human (Wh+50)", "Final (S)")
        val entries = listOf(
            RadarEntry(wa),
            RadarEntry(wh + 50f),  // offset so chart shows properly even with negative Wh
            RadarEntry(score)
        )

        val dataSet = RadarDataSet(entries, "Trust Components").apply {
            setColor(color)
            fillColor = color
            setDrawFilled(true)
            fillAlpha = 80
            lineWidth = 2f
            setDrawValues(false)
            isDrawHighlightCircleEnabled = true
        }

        chart.data = RadarData(dataSet)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.webColor = Color.parseColor("#37474F")
        chart.webColorInner = Color.parseColor("#37474F")
        chart.yAxis.setDrawLabels(false)
        chart.yAxis.axisMaximum = 100f
        chart.yAxis.axisMinimum = 0f
        chart.animateXY(600, 600)
        chart.invalidate()
    }
}
