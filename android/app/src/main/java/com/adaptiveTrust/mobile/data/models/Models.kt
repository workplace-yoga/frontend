package com.adaptiveTrust.mobile.data.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

// ── Status Badge ───────────────────────────────────────────────────────────────
data class StatusBadge(
    val label: String,
    @SerializedName("color_hex") val colorHex: String
)

// ── User Flags ─────────────────────────────────────────────────────────────────
data class UserFlags(
    @SerializedName("impossible_travel") val impossibleTravel: Boolean,
    @SerializedName("failed_logins") val failedLogins: Boolean,
    @SerializedName("unknown_device") val unknownDevice: Boolean
)

// ── User Summary Item (Glanceable Feed) ───────────────────────────────────────
data class UserSummaryItem(
    @SerializedName("user_id") val userId: String,
    val username: String,
    val department: String?,
    @SerializedName("final_score") val finalScore: Int,
    @SerializedName("base_score") val baseScore: Int,
    @SerializedName("human_adjustment") val humanAdjustment: Int,
    val status: String,
    @SerializedName("status_badge") val statusBadge: StatusBadge,
    val flags: UserFlags,
    @SerializedName("last_location") val lastLocation: String?,
    @SerializedName("last_updated") val lastUpdated: String
)

// ── Users Summary Response ─────────────────────────────────────────────────────
data class UserSummaryResponse(
    val total: Int,
    @SerializedName("red_count") val redCount: Int,
    @SerializedName("yellow_count") val yellowCount: Int,
    @SerializedName("green_count") val greenCount: Int,
    val users: List<UserSummaryItem>
)

// ── Nudge History ──────────────────────────────────────────────────────────────
data class NudgeHistoryItem(
    val action: String,
    @SerializedName("wh_delta") val whDelta: Int,
    val reason: String?,
    @SerializedName("applied_at") val appliedAt: String,
    @SerializedName("admin_username") val adminUsername: String,
    @SerializedName("biometric_verified") val biometricVerified: Boolean
)

// ── User Detail ────────────────────────────────────────────────────────────────
data class UserDetailResponse(
    @SerializedName("user_id") val userId: String,
    val username: String,
    val email: String?,
    val department: String?,
    @SerializedName("final_score") val finalScore: Int,
    @SerializedName("base_score") val baseScore: Int,
    @SerializedName("human_adjustment") val humanAdjustment: Int,
    val status: String,
    @SerializedName("status_badge") val statusBadge: StatusBadge,
    val flags: UserFlags,
    @SerializedName("last_location_city") val lastLocationCity: String?,
    @SerializedName("last_location_country") val lastLocationCountry: String?,
    @SerializedName("last_ip") val lastIp: String?,
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("nudge_history") val nudgeHistory: List<NudgeHistoryItem>
)

// ── Nudge ──────────────────────────────────────────────────────────────────────
data class NudgeRequest(
    @SerializedName("user_id") val userId: String,
    val action: String,            // "Verify" | "Approve" | "Suspend"
    val reason: String?,
    @SerializedName("biometric_token") val biometricToken: String
)

data class NudgeResponse(
    @SerializedName("user_id") val userId: String,
    val action: String,
    @SerializedName("wh_delta") val whDelta: Int,
    @SerializedName("new_final_score") val newFinalScore: Int,
    @SerializedName("new_status") val newStatus: String,
    @SerializedName("status_badge") val statusBadge: StatusBadge,
    val message: String
)

// ── Sensitivity ────────────────────────────────────────────────────────────────
data class SensitivityResponse(
    val level: Int,
    @SerializedName("red_threshold") val redThreshold: Int,
    @SerializedName("yellow_threshold") val yellowThreshold: Int,
    @SerializedName("auto_suspend_enabled") val autoSuspendEnabled: Boolean,
    @SerializedName("level_description") val levelDescription: String
)

data class SensitivityUpdateRequest(
    val level: Int,
    @SerializedName("auto_suspend_enabled") val autoSuspendEnabled: Boolean?,
    @SerializedName("biometric_token") val biometricToken: String
)

// ── Alert ──────────────────────────────────────────────────────────────────────
data class AlertMessage(
    @SerializedName("alert_id") val alertId: Long,
    @SerializedName("user_id") val userId: String,
    val severity: String,
    val message: String,
    @SerializedName("rule_triggered") val ruleTriggered: String?,
    @SerializedName("created_at") val createdAt: String,
    val type: String = "alert"
)

// ── Auth & Registration ─────────────────────────────────────────────────────────
data class RegisterRequest(
    @SerializedName("company_name") val companyName: String,
    @SerializedName("admin_username") val adminUsername: String,
    @SerializedName("admin_email") val adminEmail: String,
    @SerializedName("admin_password") val adminPassword: String
)

data class RegisterResponse(
    val message: String,
    @SerializedName("company_id") val companyId: String,
    @SerializedName("admin_id") val adminId: String
)

// ── Auth ───────────────────────────────────────────────────────────────────────
data class LoginRequest(
    val username: String,
    val password: String
)

data class OtpVerifyRequest(
    val username: String,
    val otp: String
)

data class PreAuthResponse(
    val message: String,
    val username: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val role: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// ── Console Event ──────────────────────────────────────────────────────────────
data class ConsoleEvent(
    @SerializedName("event_type") val eventType: String,
    val timestamp: String,
    val message: String,
    val severity: String = "INFO"
)

// ── Integrations ───────────────────────────────────────────────────────────────
data class IntegrationResponse(
    @SerializedName("wazuh_api_url") val wazuhApiUrl: String?,
    @SerializedName("wazuh_api_user") val wazuhApiUser: String?,
    @SerializedName("keycloak_url") val keycloakUrl: String?,
    @SerializedName("keycloak_realm") val keycloakRealm: String?,
    @SerializedName("keycloak_client_id") val keycloakClientId: String?,
    @SerializedName("simulate_data") val simulateData: Boolean
)

data class IntegrationUpdateRequest(
    @SerializedName("wazuh_api_url") val wazuhApiUrl: String?,
    @SerializedName("wazuh_api_user") val wazuhApiUser: String?,
    @SerializedName("wazuh_api_pass") val wazuhApiPass: String?,
    @SerializedName("keycloak_url") val keycloakUrl: String?,
    @SerializedName("keycloak_realm") val keycloakRealm: String?,
    @SerializedName("keycloak_client_id") val keycloakClientId: String?,
    @SerializedName("simulate_data") val simulateData: Boolean?
)

// ── User Management ────────────────────────────────────────────────────────────
data class AddUserRequest(
    val username: String,
    val email: String?,
    val department: String?,
    @SerializedName("biometric_token") val biometricToken: String
)

data class RemoveUserRequest(
    @SerializedName("biometric_token") val biometricToken: String
)

data class BasicMessageResponse(
    val message: String
)
