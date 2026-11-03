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
import com.adaptiveTrust.mobile.ui.BiometricHelper
import com.adaptiveTrust.mobile.viewmodel.SensitivityViewModel
import com.adaptiveTrust.mobile.viewmodel.UiState
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint

/**
 * SCREEN 3: Sensitivity Slider
 *
 * 7-level global sensitivity control:
 *   1 = Lenient → 7 = Paranoid
 * Changing the level requires Super_Admin + biometric auth.
 */
@AndroidEntryPoint
class SensitivityFragment : Fragment() {

    private val vm: SensitivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sensitivity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slider        = view.findViewById<Slider>(R.id.slider_sensitivity)
        val tvLevel       = view.findViewById<TextView>(R.id.tv_sensitivity_level)
        val tvDescription = view.findViewById<TextView>(R.id.tv_sensitivity_desc)
        val tvRedThresh   = view.findViewById<TextView>(R.id.tv_red_threshold)
        val tvYellowThresh= view.findViewById<TextView>(R.id.tv_yellow_threshold)
        val switchAutoSuspend = view.findViewById<Switch>(R.id.switch_auto_suspend)
        val btnApply      = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_apply_sensitivity)
        val progressBar   = view.findViewById<ProgressBar>(R.id.progress_sensitivity)
        val tvResult      = view.findViewById<TextView>(R.id.tv_sensitivity_result)

        // Level color map
        val levelColors = mapOf(
            1 to "#4CAF50", 2 to "#8BC34A", 3 to "#CDDC39",
            4 to "#FFD600", 5 to "#FF9800", 6 to "#FF5722", 7 to "#F44336"
        )

        slider.valueFrom = 1f
        slider.valueTo   = 7f
        slider.stepSize  = 1f

        slider.addOnChangeListener { _, value, fromUser ->
            val level = value.toInt()
            val desc  = vm.levelDescriptions[level] ?: "Balanced"
            val color = Color.parseColor(levelColors[level] ?: "#FFD600")
            tvLevel.text = "Level $level"
            tvLevel.setTextColor(color)
            tvDescription.text = desc
        }

        btnApply.setOnClickListener {
            val level = slider.value.toInt()
            if (!BiometricHelper.isAvailable(requireActivity())) {
                Toast.makeText(context, "Biometric not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            BiometricHelper.authenticate(
                activity = requireActivity(),
                title = "Confirm Sensitivity Change",
                subtitle = "Level $level – ${vm.levelDescriptions[level]}",
                description = "Changing system sensitivity affects all users globally.",
                onSuccess = { token ->
                    vm.update(level, switchAutoSuspend.isChecked, token)
                },
                onFailure = { err ->
                    tvResult.text = "❌ $err"
                    tvResult.setTextColor(Color.parseColor("#FF1744"))
                    tvResult.visibility = View.VISIBLE
                }
            )
        }

        vm.sensitivityState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> progressBar.visibility = View.VISIBLE
                is UiState.Error   -> {
                    progressBar.visibility = View.GONE
                    tvResult.text = "Error: ${state.message}"
                    tvResult.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    val cfg = state.data
                    slider.value = cfg.level.toFloat()
                    tvLevel.text = "Level ${cfg.level}"
                    tvDescription.text = cfg.levelDescription
                    tvRedThresh.text   = "Red threshold: < ${cfg.redThreshold}"
                    tvYellowThresh.text= "Yellow threshold: ${cfg.redThreshold}–${cfg.yellowThreshold}"
                    switchAutoSuspend.isChecked = cfg.autoSuspendEnabled
                    val color = Color.parseColor(levelColors[cfg.level] ?: "#FFD600")
                    tvLevel.setTextColor(color)
                }
            }
        }

        vm.updateResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvResult.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvResult.text = "✅ Sensitivity updated to Level ${state.data.level} (${state.data.levelDescription})"
                    tvResult.setTextColor(Color.parseColor("#00E676"))
                    tvResult.visibility = View.VISIBLE
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvResult.text = "❌ ${state.message}"
                    tvResult.setTextColor(Color.parseColor("#FF1744"))
                    tvResult.visibility = View.VISIBLE
                }
            }
        }

        vm.load()
    }
}
