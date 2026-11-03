package com.adaptiveTrust.mobile.data.api

import com.adaptiveTrust.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit2 interface – mirrors FastAPI REST endpoints.
 * All calls are suspend functions for coroutine support.
 */
interface AdaptiveTrustApi {

    // ── Auth ───────────────────────────────────────────────────────────────────
    @POST("api/v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/auth/token")
    suspend fun requestOtp(@Body request: LoginRequest): Response<PreAuthResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<TokenResponse>

    // ── Users ──────────────────────────────────────────────────────────────────
    @GET("api/v1/users/summary")
    suspend fun getUsersSummary(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("status") statusFilter: String? = null
    ): Response<UserSummaryResponse>

    @GET("api/v1/users/{user_id}")
    suspend fun getUserDetail(
        @Path("user_id") userId: String
    ): Response<UserDetailResponse>

    // ── Nudge ──────────────────────────────────────────────────────────────────
    @POST("api/v1/nudge")
    suspend fun applyNudge(@Body request: NudgeRequest): Response<NudgeResponse>

    // ── Sensitivity ────────────────────────────────────────────────────────────
    @GET("api/v1/sensitivity")
    suspend fun getSensitivity(): Response<SensitivityResponse>

    @PUT("api/v1/sensitivity")
    suspend fun updateSensitivity(
        @Body request: SensitivityUpdateRequest
    ): Response<SensitivityResponse>

    // ── Integrations ───────────────────────────────────────────────────────────
    @GET("api/v1/integrations")
    suspend fun getIntegrations(): Response<IntegrationResponse>

    @PUT("api/v1/integrations")
    suspend fun updateIntegrations(
        @Body request: IntegrationUpdateRequest
    ): Response<IntegrationResponse>

    // ── User Management ────────────────────────────────────────────────────────
    @POST("api/v1/users")
    suspend fun addUser(@Body request: AddUserRequest): Response<BasicMessageResponse>

    @POST("api/v1/users/{user_id}/remove")
    suspend fun removeUser(
        @Path("user_id") userId: String,
        @Body request: RemoveUserRequest
    ): Response<BasicMessageResponse>

    // ── Alerts (REST fallback) ─────────────────────────────────────────────────
    @GET("api/v1/alerts")
    suspend fun getAlerts(
        @Query("limit") limit: Int = 50,
        @Query("unacknowledged_only") unacknowledgedOnly: Boolean = false
    ): Response<List<AlertMessage>>

    // ── Health ─────────────────────────────────────────────────────────────────
    @GET("api/v1/health")
    suspend fun health(): Response<Map<String, String>>
}
