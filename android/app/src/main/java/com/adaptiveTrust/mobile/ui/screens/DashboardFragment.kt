package com.adaptiveTrust.mobile.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adaptiveTrust.mobile.R
import com.adaptiveTrust.mobile.data.models.UserSummaryItem
import com.adaptiveTrust.mobile.data.models.AddUserRequest
import com.adaptiveTrust.mobile.ui.BiometricHelper
import com.adaptiveTrust.mobile.viewmodel.DashboardViewModel
import com.adaptiveTrust.mobile.viewmodel.UiState
import dagger.hilt.android.AndroidEntryPoint

/**
 * SCREEN 1: Glanceable Feed
 * Color-coded user list sorted by lowest trust score.
 *   Red   < 40  → #FF1744
 *   Yellow 40–60 → #FFD600
 *   Green > 60  → #00E676
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val vm: DashboardViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_users)
        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        val tvGlobalScore = view.findViewById<TextView>(R.id.tv_global_score)
        val tvRiskStatus = view.findViewById<TextView>(R.id.tv_risk_status)
        val viewRiskRed = view.findViewById<View>(R.id.view_risk_red)
        val viewRiskYellow = view.findViewById<View>(R.id.view_risk_yellow)
        val viewRiskGreen = view.findViewById<View>(R.id.view_risk_green)

        val tvRedCount = view.findViewById<TextView>(R.id.tv_red_count)
        val tvYellowCount = view.findViewById<TextView>(R.id.tv_yellow_count)
        val tvGreenCount = view.findViewById<TextView>(R.id.tv_green_count)
        val chipAll = view.findViewById<com.google.android.material.chip.Chip>(R.id.chip_all)
        val chipRed = view.findViewById<com.google.android.material.chip.Chip>(R.id.chip_red)
        val chipYellow = view.findViewById<com.google.android.material.chip.Chip>(R.id.chip_yellow)
        val chipGreen = view.findViewById<com.google.android.material.chip.Chip>(R.id.chip_green)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val tvError = view.findViewById<TextView>(R.id.tv_error)
        val layoutEmptyState = view.findViewById<View>(R.id.layout_empty_state)
        val btnLogout = view.findViewById<ImageView>(R.id.btn_logout)

        btnLogout.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("End Session")
                .setMessage("Are you sure you want to end the admin session?")
                .setPositiveButton("Logout") { _, _ ->
                    vm.logout()
                    startActivity(android.content.Intent(requireContext(), com.adaptiveTrust.mobile.ui.LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        adapter = UserAdapter { user ->
            val action = DashboardFragmentDirections.actionDashboardToUserDetail(user.userId)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Filter chips
        chipAll.setOnClickListener { vm.setStatusFilter(null) }
        chipRed.setOnClickListener { vm.setStatusFilter("Red") }
        chipYellow.setOnClickListener { vm.setStatusFilter("Yellow") }
        chipGreen.setOnClickListener { vm.setStatusFilter("Green") }

        val fabAddUser = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_user)
        fabAddUser.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null)
            val etUsername = dialogView.findViewById<EditText>(R.id.et_username)
            val etEmail = dialogView.findViewById<EditText>(R.id.et_email)
            val etDepartment = dialogView.findViewById<EditText>(R.id.et_department)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Verify & Add") { _, _ ->
                    val username = etUsername.text.toString()
                    val email = etEmail.text.toString()
                    val department = etDepartment.text.toString()

                    if (username.isBlank()) {
                        Toast.makeText(context, "Username required", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (!BiometricHelper.isAvailable(requireActivity())) {
                        Toast.makeText(context, "Biometrics unavailable", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    BiometricHelper.authenticate(
                        activity = requireActivity(),
                        title = "Verify Admin Identity",
                        subtitle = "Confirm addition of $username",
                        onSuccess = { token ->
                            val request = AddUserRequest(
                                username = username,
                                email = email.takeIf { it.isNotBlank() },
                                department = department.takeIf { it.isNotBlank() },
                                biometricToken = token
                            )
                            vm.addUser(
                                request,
                                onSuccess = { Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show() },
                                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                            )
                        },
                        onFailure = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        swipeRefresh.setOnRefreshListener { vm.refresh() }
        swipeRefresh.setColorSchemeColors(
            Color.parseColor("#FF1744"),
            Color.parseColor("#FFD600"),
            Color.parseColor("#00E676")
        )

        vm.refreshState.observe(viewLifecycleOwner) { state ->
            swipeRefresh.isRefreshing = false
            when (state) {
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        vm.users.observe(viewLifecycleOwner) { cachedUsers ->
            if (cachedUsers.isEmpty()) {
                recyclerView.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            }

            val mapped = cachedUsers.map { u ->
                UserSummaryItem(
                    userId = u.userId,
                    username = u.username,
                    department = u.department,
                    finalScore = u.finalScore,
                    baseScore = u.baseScore,
                    humanAdjustment = u.humanAdjustment,
                    status = u.status,
                    statusBadge = com.adaptiveTrust.mobile.data.models.StatusBadge(u.status, u.statusColorHex),
                    flags = com.adaptiveTrust.mobile.data.models.UserFlags(u.flagImpossibleTravel, u.flagFailedLogins, u.flagUnknownDevice),
                    lastLocation = u.lastLocation,
                    lastUpdated = u.lastUpdated
                )
            }
            adapter.submitList(mapped)
            
            val redCount = cachedUsers.count { it.status == "Red" }
            val yellowCount = cachedUsers.count { it.status == "Yellow" }
            val greenCount = cachedUsers.count { it.status == "Green" }
            val total = cachedUsers.size.coerceAtLeast(1)

            tvRedCount.text = "$redCount"
            tvYellowCount.text = "$yellowCount"
            tvGreenCount.text = "$greenCount"
            
            // Calculate Global Score (Average)
            val avgScore = if (cachedUsers.isNotEmpty()) cachedUsers.map { it.finalScore }.average().toInt() else 0
            tvGlobalScore.text = "$avgScore"
            
            // Update Risk Level Meter
            val redWeight = (redCount.toFloat() / total * 100).toInt().coerceAtLeast(1)
            val yellowWeight = (yellowCount.toFloat() / total * 100).toInt().coerceAtLeast(1)
            val greenWeight = (100 - redWeight - yellowWeight).coerceAtLeast(1)
            
            viewRiskRed.layoutParams = (viewRiskRed.layoutParams as android.widget.LinearLayout.LayoutParams).apply { weight = redWeight.toFloat() }
            viewRiskYellow.layoutParams = (viewRiskYellow.layoutParams as android.widget.LinearLayout.LayoutParams).apply { weight = yellowWeight.toFloat() }
            viewRiskGreen.layoutParams = (viewRiskGreen.layoutParams as android.widget.LinearLayout.LayoutParams).apply { weight = greenWeight.toFloat() }
            
            if (redCount > 0) {
                tvRiskStatus.text = "CRITICAL"
                tvRiskStatus.setTextColor(Color.parseColor("#FF1744"))
            } else if (yellowCount > 0) {
                tvRiskStatus.text = "ELEVATED"
                tvRiskStatus.setTextColor(Color.parseColor("#FFD600"))
            } else {
                tvRiskStatus.text = "STABLE"
                tvRiskStatus.setTextColor(Color.parseColor("#00E676"))
            }
        }
    }
}

// ── RecyclerView Adapter ───────────────────────────────────────────────────────
class UserAdapter(
    private val onClick: (UserSummaryItem) -> Unit
) : ListAdapter<UserSummaryItem, UserAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<UserSummaryItem>() {
            override fun areItemsTheSame(a: UserSummaryItem, b: UserSummaryItem) = a.userId == b.userId
            override fun areContentsTheSame(a: UserSummaryItem, b: UserSummaryItem) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val tvDept: TextView = view.findViewById(R.id.tv_department)
        val tvScore: TextView = view.findViewById(R.id.tv_score)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val statusBar: View = view.findViewById(R.id.status_color_bar)
        val ivTravel: ImageView = view.findViewById(R.id.iv_flag_travel)
        val ivLogins: ImageView = view.findViewById(R.id.iv_flag_logins)
        val ivDevice: ImageView = view.findViewById(R.id.iv_flag_device)
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        val color = Color.parseColor(user.statusBadge.colorHex)

        holder.tvUsername.text = user.username
        holder.tvDept.text = user.department ?: "Unknown"
        holder.tvScore.text = "${user.finalScore}"
        holder.tvScore.setTextColor(color)
        holder.tvStatus.text = user.status
        holder.tvStatus.setTextColor(color)
        holder.statusBar.setBackgroundColor(color)
        holder.tvLocation.text = user.lastLocation ?: "—"

        // Flag indicators
        holder.ivTravel.visibility = if (user.flags.impossibleTravel) View.VISIBLE else View.GONE
        holder.ivLogins.visibility = if (user.flags.failedLogins) View.VISIBLE else View.GONE
        holder.ivDevice.visibility = if (user.flags.unknownDevice) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onClick(user) }

        // Pulse animation for RED users
        if (user.status == "Red") {
            startPulseAnimation(holder.statusBar)
        } else {
            holder.statusBar.clearAnimation()
        }
    }

    private fun startPulseAnimation(view: View) {
        val anim = android.view.animation.AlphaAnimation(0.3f, 1.0f).apply {
            duration = 800
            repeatCount = android.view.animation.Animation.INFINITE
            repeatMode = android.view.animation.Animation.REVERSE
        }
        view.startAnimation(anim)
    }
}
