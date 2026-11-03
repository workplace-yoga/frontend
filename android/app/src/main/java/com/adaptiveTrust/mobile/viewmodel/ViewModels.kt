package com.adaptiveTrust.mobile.viewmodel

import androidx.lifecycle.*
import com.adaptiveTrust.mobile.data.local.*
import com.adaptiveTrust.mobile.data.local.TokenStore
import com.adaptiveTrust.mobile.data.models.*
import com.adaptiveTrust.mobile.data.repository.TrustRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// ─────────────────────────────────────────────────────────────────────────────
// DashboardViewModel – Glanceable Feed (offline-first via Room)
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TrustRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    /** Live Room flow — survives network outages. */
    val users: LiveData<List<CachedUser>> = repository.observeUsers()
        .catch { emit(emptyList()) }
        .asLiveData()

    val redCount: LiveData<Int> = repository.observeRedCount()
        .catch { emit(0) }
        .asLiveData()

    private val _refreshState = MutableLiveData<UiState<Unit>>(UiState.Success(Unit))
    val refreshState: LiveData<UiState<Unit>> = _refreshState

    private val _statusFilter = MutableLiveData<String?>(null)
    val statusFilter: LiveData<String?> = _statusFilter

    fun refresh(statusFilter: String? = _statusFilter.value) {
        _refreshState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.refreshUsers(statusFilter)
            _refreshState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Network error") }
            )
        }
    }

    fun setStatusFilter(filter: String?) {
        _statusFilter.value = filter
        refresh(filter)
    }

    /** Initial load on VM creation. */
    init { refresh() }

    /** 
     * Alias for refresh() to resolve 'Unresolved reference: loadUsers'.
     * This ensures any calls to loadUsers() from the Fragment work flawlessly
     * while keeping our advanced Room database offline-first architecture intact.
     */
    fun loadUsers() {
        refresh()
    }

    fun logout() {
        viewModelScope.launch { tokenStore.clearToken() }
    }

    fun addUser(request: AddUserRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.addUser(request).fold(
                onSuccess = {
                    refresh() // Refresh the list after adding
                    onSuccess()
                },
                onFailure = { onError(it.message ?: "Failed to add user") }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UserDetailViewModel – Nudge Panel
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val repository: TrustRepository
) : ViewModel() {

    private val _userState = MutableLiveData<UiState<UserDetailResponse>>()
    val userState: LiveData<UiState<UserDetailResponse>> = _userState

    private val _nudgeResult = MutableLiveData<UiState<NudgeResponse>>()
    val nudgeResult: LiveData<UiState<NudgeResponse>> = _nudgeResult

    /** Local nudge history for this user from Room (instant, no network). */
    private var _userId = MutableLiveData<String>()

    val nudgeHistory: LiveData<List<NudgeLogEntry>> = _userId.switchMap { uid ->
        repository.observeNudgeLogForUser(uid).asLiveData()
    }

    fun loadUser(userId: String) {
        _userId.value = userId
        _userState.value = UiState.Loading
        viewModelScope.launch {
            repository.getUserDetail(userId).fold(
                onSuccess  = { _userState.value = UiState.Success(it) },
                onFailure  = { _userState.value = UiState.Error(it.localizedMessage ?: "Error") }
            )
        }
    }

    /**
     * Submit a nudge. [biometricToken] MUST be provided (obtained after
     * BiometricPrompt succeeds — see BiometricHelper).
     * Action: "Verify" (+20) | "Approve" (+15) | "Suspend" (-100)
     */
    fun applyNudge(userId: String, action: String, reason: String?, biometricToken: String) {
        _nudgeResult.value = UiState.Loading
        viewModelScope.launch {
            repository.applyNudge(
                NudgeRequest(userId, action, reason, biometricToken)
            ).fold(
                onSuccess = {
                    _nudgeResult.value = UiState.Success(it)
                    loadUser(userId) // refresh detail
                },
                onFailure = {
                    _nudgeResult.value = UiState.Error(it.localizedMessage ?: "Nudge failed")
                }
            )
        }
    }

    fun removeUser(userId: String, request: RemoveUserRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.removeUser(userId, request).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to remove user") }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SensitivityViewModel – 7-Level Slider
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class SensitivityViewModel @Inject constructor(
    private val repository: TrustRepository
) : ViewModel() {

    private val _sensitivityState = MutableLiveData<UiState<SensitivityResponse>>()
    val sensitivityState: LiveData<UiState<SensitivityResponse>> = _sensitivityState

    private val _updateResult = MutableLiveData<UiState<SensitivityResponse>>()
    val updateResult: LiveData<UiState<SensitivityResponse>> = _updateResult

    val levelDescriptions = mapOf(
        1 to "LENIENT",  2 to "PERMISSIVE", 3 to "BALANCED-LOW",
        4 to "BALANCED", 5 to "STRICT",     6 to "AGGRESSIVE",  7 to "PARANOID"
    )

    val levelSubtitles = mapOf(
        1 to "All users pass — monitor only",
        2 to "Block only critical threats",
        3 to "Block Red users automatically",
        4 to "Block Red + warn Yellow",
        5 to "Strict: block < 50 score",
        6 to "Aggressive: block < 60 score",
        7 to "Zero-tolerance: all anomalies blocked"
    )

    fun load() {
        _sensitivityState.value = UiState.Loading
        viewModelScope.launch {
            repository.getSensitivity().fold(
                onSuccess  = { _sensitivityState.value = UiState.Success(it) },
                onFailure  = { _sensitivityState.value = UiState.Error("${it.message}") }
            )
        }
    }

    fun update(level: Int, autoSuspend: Boolean, biometricToken: String) {
        _updateResult.value = UiState.Loading
        viewModelScope.launch {
            repository.updateSensitivity(
                SensitivityUpdateRequest(level, autoSuspend, biometricToken)
            ).fold(
                onSuccess = {
                    _updateResult.value  = UiState.Success(it)
                    _sensitivityState.value = UiState.Success(it)
                },
                onFailure = {
                    _updateResult.value = UiState.Error("Update failed: ${it.message}")
                }
            )
        }
    }

    init { load() }
}

// ─────────────────────────────────────────────────────────────────────────────
// AlertsViewModel – Real-time Console + Cached Alerts (Room-backed)
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: TrustRepository
) : ViewModel() {

    /** Room-backed alert stream — persists across app restarts. */
    val alerts: LiveData<List<CachedAlert>> = repository.observeAlerts()
        .catch { emit(emptyList()) }
        .asLiveData()

    val unacknowledgedCount: LiveData<Int> = repository.observeUnacknowledgedCount()
        .catch { emit(0) }
        .asLiveData()

    private val _consoleLog = MutableLiveData<List<ConsoleEvent>>(emptyList())
    val consoleLog: LiveData<List<ConsoleEvent>> = _consoleLog

    fun addConsoleEvent(event: ConsoleEvent) {
        val current = _consoleLog.value.orEmpty().toMutableList()
        current.add(0, event)
        if (current.size > 500) current.removeAt(current.lastIndex)
        _consoleLog.postValue(current)
    }

    /** Called when a WebSocket alert arrives — writes to Room. */
    fun ingestAlert(alert: AlertMessage) {
        viewModelScope.launch {
            repository.cacheAlert(alert)
        }
        // Also emit to console log
        addConsoleEvent(ConsoleEvent(
            eventType = "ALERT",
            timestamp = alert.createdAt,
            message = "[${alert.severity}] ${alert.message}",
            severity = alert.severity
        ))
    }

    fun acknowledgeAlert(id: Long) {
        viewModelScope.launch { repository.acknowledgeAlert(id) }
    }

    fun acknowledgeAll() {
        viewModelScope.launch { repository.acknowledgeAllAlerts() }
    }

    fun loadHistorical() {
        viewModelScope.launch { repository.loadHistoricalAlerts() }
    }

    init { loadHistorical() }
}

// ─────────────────────────────────────────────────────────────────────────────
// LoginViewModel
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: TrustRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _otpState = MutableLiveData<UiState<PreAuthResponse>>()
    val otpState: LiveData<UiState<PreAuthResponse>> = _otpState

    private val _loginState = MutableLiveData<UiState<TokenResponse>>()
    val loginState: LiveData<UiState<TokenResponse>> = _loginState

    private var failedAttempts = 0
    private var lockoutUntil = 0L

    fun requestOtp(username: String, password: String) {
        if (System.currentTimeMillis() < lockoutUntil) {
            val remain = (lockoutUntil - System.currentTimeMillis()) / 1000
            _otpState.value = UiState.Error("Locked out. Try again in ${remain}s")
            return
        }

        _otpState.value = UiState.Loading
        viewModelScope.launch {
            repository.requestOtp(username, password).fold(
                onSuccess = { resp ->
                    failedAttempts = 0
                    _otpState.value = UiState.Success(resp)
                },
                onFailure = {
                    failedAttempts++
                    if (failedAttempts >= 3) {
                        lockoutUntil = System.currentTimeMillis() + 30_000L
                        _otpState.value = UiState.Error("Security Lockout: 3 failed attempts. Wait 30s.")
                    } else {
                        _otpState.value = UiState.Error(it.localizedMessage ?: "Auth error")
                    }
                }
            )
        }
    }

    fun verifyOtp(username: String, otp: String) {
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            repository.verifyOtp(username, otp).fold(
                onSuccess = { token ->
                    tokenStore.saveToken(token.accessToken, token.role, username)
                    _loginState.value = UiState.Success(token)
                },
                onFailure = {
                    _loginState.value = UiState.Error(it.localizedMessage ?: "Invalid OTP")
                }
            )
        }
    }

    private val _registerState = MutableLiveData<UiState<RegisterResponse>>()
    val registerState: LiveData<UiState<RegisterResponse>> = _registerState

    fun registerCompany(companyName: String, adminUser: String, adminEmail: String, adminPass: String) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            repository.registerCompany(RegisterRequest(companyName, adminUser, adminEmail, adminPass)).fold(
                onSuccess = { _registerState.value = UiState.Success(it) },
                onFailure = { _registerState.value = UiState.Error(it.localizedMessage ?: "Register error") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch { tokenStore.clearToken() }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// IntegrationsViewModel – Data Sources
// ─────────────────────────────────────────────────────────────────────────────
@HiltViewModel
class IntegrationsViewModel @Inject constructor(
    private val repository: TrustRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState<IntegrationResponse>>()
    val state: LiveData<UiState<IntegrationResponse>> = _state

    private val _updateResult = MutableLiveData<UiState<IntegrationResponse>>()
    val updateResult: LiveData<UiState<IntegrationResponse>> = _updateResult

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.getIntegrations().fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error("${it.message}") }
            )
        }
    }

    fun update(request: IntegrationUpdateRequest) {
        _updateResult.value = UiState.Loading
        viewModelScope.launch {
            repository.updateIntegrations(request).fold(
                onSuccess = {
                    _updateResult.value = UiState.Success(it)
                    _state.value = UiState.Success(it)
                },
                onFailure = {
                    _updateResult.value = UiState.Error("Failed to save: ${it.message}")
                }
            )
        }
    }

    init { load() }
}
