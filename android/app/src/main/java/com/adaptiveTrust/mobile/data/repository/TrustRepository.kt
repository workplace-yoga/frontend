package com.adaptiveTrust.mobile.data.repository

import com.adaptiveTrust.mobile.data.api.AdaptiveTrustApi
import com.adaptiveTrust.mobile.data.local.*
import com.adaptiveTrust.mobile.data.models.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the app.
 * - Fetches from network, writes to Room, exposes Flows for UI observation.
 * - On network failure, stale Room data is served (offline-first).
 */
@Singleton
class TrustRepository @Inject constructor(
    private val api: AdaptiveTrustApi,
    private val userCacheDao: UserCacheDao,
    private val alertCacheDao: AlertCacheDao,
    private val nudgeLogDao: NudgeLogDao
) {

    // ── Users ─────────────────────────────────────────────────────────────────

    /** Live user feed – always served from Room, refreshed from network. */
    fun observeUsers(): Flow<List<CachedUser>> = userCacheDao.observeAll()

    fun observeUsersByStatus(status: String): Flow<List<CachedUser>> =
        userCacheDao.observeByStatus(status)

    fun observeRedCount(): Flow<Int> = userCacheDao.observeRedCount()

    /**
     * Refresh users from network and write to cache.
     * Returns Result so the ViewModel can show error state without crashing flow.
     */
    suspend fun refreshUsers(statusFilter: String? = null): Result<UserSummaryResponse> {
        return try {
            val resp = api.getUsersSummary(limit = 100, statusFilter = statusFilter)
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                val entities = body.users.map { it.toCachedUser() }
                userCacheDao.upsertAll(entities)
                // Trim stale entries older than 30 minutes
                userCacheDao.deleteStale(System.currentTimeMillis() - 30 * 60 * 1000L)
                Result.success(body)
            } else {
                Result.failure(Exception("Server error ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetail(userId: String): Result<UserDetailResponse> {
        return try {
            val resp = api.getUserDetail(userId)
            if (resp.isSuccessful && resp.body() != null) {
                // Update the cached summary for this user too
                val body = resp.body()!!
                userCacheDao.upsert(body.toCachedUser())
                Result.success(body)
            } else {
                Result.failure(Exception("User not found: ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addUser(request: AddUserRequest): Result<String> {
        return try {
            val resp = api.addUser(request)
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!.message)
            else Result.failure(Exception("Failed to add user: ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeUser(userId: String, request: RemoveUserRequest): Result<String> {
        return try {
            val resp = api.removeUser(userId, request)
            if (resp.isSuccessful && resp.body() != null) {
                // Remove from local cache immediately
                userCacheDao.deleteById(userId)
                Result.success(resp.body()!!.message)
            } else {
                Result.failure(Exception("Failed to remove user: ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Nudges ────────────────────────────────────────────────────────────────

    suspend fun applyNudge(request: NudgeRequest): Result<NudgeResponse> {
        return try {
            val resp = api.applyNudge(request)
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                // Log nudge locally for audit
                nudgeLogDao.insert(
                    NudgeLogEntry(
                        userId = body.userId,
                        username = request.userId, // resolved by caller if needed
                        action = body.action,
                        whDelta = body.whDelta,
                        reason = request.reason,
                        newScore = body.newFinalScore,
                        newStatus = body.newStatus,
                        biometricVerified = true
                    )
                )
                // Update local cache immediately for optimistic UI
                val cached = userCacheDao.getById(body.userId)
                if (cached != null) {
                    userCacheDao.upsert(
                        cached.copy(
                            finalScore = body.newFinalScore,
                            status = body.newStatus,
                            statusColorHex = body.statusBadge.colorHex
                        )
                    )
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Nudge failed: ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeNudgeLog(): Flow<List<NudgeLogEntry>> = nudgeLogDao.observeAll()

    fun observeNudgeLogForUser(userId: String): Flow<List<NudgeLogEntry>> =
        nudgeLogDao.observeForUser(userId)

    // ── Alerts ────────────────────────────────────────────────────────────────

    fun observeAlerts(): Flow<List<CachedAlert>> = alertCacheDao.observeRecent()

    fun observeUnacknowledgedAlerts(): Flow<List<CachedAlert>> =
        alertCacheDao.observeUnacknowledged()

    fun observeUnacknowledgedCount(): Flow<Int> =
        alertCacheDao.observeUnacknowledgedCount()

    /** Persist an incoming WebSocket alert to Room. */
    suspend fun cacheAlert(alert: AlertMessage) {
        alertCacheDao.insert(
            CachedAlert(
                alertId = alert.alertId,
                userId = alert.userId,
                severity = alert.severity,
                message = alert.message,
                ruleTriggered = alert.ruleTriggered,
                createdAt = alert.createdAt
            )
        )
        alertCacheDao.trimToLimit(200)
    }

    suspend fun acknowledgeAlert(id: Long) = alertCacheDao.acknowledge(id)
    suspend fun acknowledgeAllAlerts() = alertCacheDao.acknowledgeAll()

    /** Seed from REST endpoint on first launch / reconnect. */
    suspend fun loadHistoricalAlerts() {
        try {
            val resp = api.getAlerts(limit = 100)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.forEach { cacheAlert(it) }
            }
        } catch (_: Exception) {}
    }

    // ── Sensitivity ───────────────────────────────────────────────────────────

    suspend fun getSensitivity(): Result<SensitivityResponse> {
        return try {
            val resp = api.getSensitivity()
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSensitivity(request: SensitivityUpdateRequest): Result<SensitivityResponse> {
        return try {
            val resp = api.updateSensitivity(request)
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("Update failed: ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Integrations ──────────────────────────────────────────────────────────

    suspend fun getIntegrations(): Result<IntegrationResponse> {
        return try {
            val resp = api.getIntegrations()
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateIntegrations(request: IntegrationUpdateRequest): Result<IntegrationResponse> {
        return try {
            val resp = api.updateIntegrations(request)
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("Update failed: ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun requestOtp(username: String, password: String): Result<PreAuthResponse> {
        return try {
            val resp = api.requestOtp(LoginRequest(username, password))
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("Invalid credentials"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(username: String, otp: String): Result<TokenResponse> {
        return try {
            val resp = api.verifyOtp(OtpVerifyRequest(username, otp))
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("Invalid or expired OTP"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerCompany(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val resp = api.register(request)
            if (resp.isSuccessful && resp.body() != null) Result.success(resp.body()!!)
            else Result.failure(Exception("Registration failed: ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mapping extensions
// ─────────────────────────────────────────────────────────────────────────────

fun UserSummaryItem.toCachedUser() = CachedUser(
    userId = userId,
    username = username,
    department = department,
    finalScore = finalScore,
    baseScore = baseScore,
    humanAdjustment = humanAdjustment,
    status = status,
    statusColorHex = statusBadge.colorHex,
    flagImpossibleTravel = flags.impossibleTravel,
    flagFailedLogins = flags.failedLogins,
    flagUnknownDevice = flags.unknownDevice,
    lastLocation = lastLocation,
    lastUpdated = lastUpdated
)

fun UserDetailResponse.toCachedUser() = CachedUser(
    userId = userId,
    username = username,
    department = department,
    finalScore = finalScore,
    baseScore = baseScore,
    humanAdjustment = humanAdjustment,
    status = status,
    statusColorHex = statusBadge.colorHex,
    flagImpossibleTravel = flags.impossibleTravel,
    flagFailedLogins = flags.failedLogins,
    flagUnknownDevice = flags.unknownDevice,
    lastLocation = lastLocationCity,
    lastUpdated = lastUpdated
)
