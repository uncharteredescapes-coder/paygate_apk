package com.paygate.smsfwd.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paygate.smsfwd.data.local.db.AppDatabase
import com.paygate.smsfwd.data.local.entity.QueuedSms
import com.paygate.smsfwd.data.prefs.PreferenceManager
import com.paygate.smsfwd.data.remote.ApiClient
import com.paygate.smsfwd.data.remote.model.PairRequest
import com.paygate.smsfwd.data.remote.model.WalletSlotInfo
import com.paygate.smsfwd.data.security.KeystoreSecretManager
import com.paygate.smsfwd.domain.filter.SmsFilter
import com.paygate.smsfwd.service.PayGateForwarderService
import com.paygate.smsfwd.service.QueueSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HealthStatus(
    val hasSmsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isBatteryExempt: Boolean = false,
    val isNetworkConnected: Boolean = true
)

sealed interface PairingUiState {
    object Idle : PairingUiState
    object InProgress : PairingUiState
    object Loading : PairingUiState
    data class Success(val message: String = "Device paired successfully") : PairingUiState
    data class Error(val message: String) : PairingUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferenceManager(application)
    private val db = AppDatabase.getInstance(application)
    private val dao = db.queuedSmsDao()
    private val secretManager = KeystoreSecretManager(application)

    val isPairedFlow: StateFlow<Boolean> = prefs.isPairedFlow
    val languageFlow: StateFlow<String> = prefs.languageFlow
    val walletsFlow: StateFlow<List<WalletSlotInfo>> = prefs.walletsFlow
    val isKioskFlow: StateFlow<Boolean> = prefs.isKioskFlow
    val hasIgnoredWarningFlow: StateFlow<Boolean> = prefs.hasIgnoredWarningFlow

    val pendingCount: StateFlow<Int> = dao.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val staleCount: StateFlow<Int> = dao.observeStaleCount(System.currentTimeMillis() - 86_400_000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val failedCount: StateFlow<Int> = dao.observeFailedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastDeliveredSms: StateFlow<QueuedSms?> = dao.observeLastDelivered()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentLogs: StateFlow<List<QueuedSms>> = dao.observeRecentLogs(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _healthStatus = MutableStateFlow(checkHealthStatus())
    val healthStatus: StateFlow<HealthStatus> = _healthStatus.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private val _pairingState = MutableStateFlow<PairingUiState>(PairingUiState.Idle)
    val pairingState: StateFlow<PairingUiState> = _pairingState.asStateFlow()

    init {
        refreshHealthStatus()
    }

    fun clearActionFeedback() {
        _actionFeedback.value = null
    }

    fun refreshHealthStatus() {
        _healthStatus.value = checkHealthStatus()
    }

    private fun checkHealthStatus(): HealthStatus {
        val app = getApplication<Application>()
        val hasSms = ContextCompat.checkSelfPermission(
            app,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                app,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val isBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(app.packageName) == true
        } else {
            true
        }

        return HealthStatus(
            hasSmsPermission = hasSms,
            hasNotificationPermission = hasNotif,
            isBatteryExempt = isBattery,
            isNetworkConnected = true
        )
    }

    fun toggleLanguage() {
        val next = if (prefs.language == "en") "bn" else "en"
        prefs.language = next
    }

    fun setLanguage(lang: String) {
        prefs.language = lang
    }

    fun triggerSyncNow() {
        viewModelScope.launch {
            _actionFeedback.value = "Draining queue..."
            val count = QueueSyncManager.drainQueue(getApplication())
            _actionFeedback.value = "Synced $count pending messages."
        }
    }

    fun triggerHeartbeatNow() {
        PayGateForwarderService.triggerHeartbeat(getApplication())
        _actionFeedback.value = "Heartbeat dispatched."
    }

    fun simulateIncomingSms(sender: String, body: String, simSlot: Int = 1) {
        viewModelScope.launch {
            val meta = SmsFilter.extractMetadata(sender, body)
            val clientMsgId = SmsFilter.generateClientMsgId(sender, body, System.currentTimeMillis(), simSlot)
            val seq = prefs.nextDeviceSeq()

            val sms = QueuedSms(
                clientMsgId = clientMsgId,
                deviceSeq = seq,
                sender = sender,
                body = body,
                receivedAt = System.currentTimeMillis(),
                simSlot = simSlot,
                refHint = meta.refHint,
                source = QueuedSms.SOURCE_LIVE,
                status = QueuedSms.STATUS_PENDING,
                attemptCount = 0,
                trxId = meta.trxId,
                amount = meta.amount,
                provider = meta.provider
            )

            dao.insertOrIgnore(sms)
            _actionFeedback.value = "Captured simulated ${meta.provider} payment (${meta.amount ?: "Tk 0"})."
            QueueSyncManager.drainQueue(getApplication())
        }
    }

    fun retryFailedItem(id: Long) {
        viewModelScope.launch {
            dao.retryItem(id)
            QueueSyncManager.drainQueue(getApplication())
        }
    }

    fun pairDevice(code: String, baseUrl: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 4) {
            _pairingState.value = PairingUiState.Error("Invalid pairing code. Minimum 4 characters required.")
            return
        }

        viewModelScope.launch {
            _pairingState.value = PairingUiState.InProgress
            try {
                prefs.apiBaseUrl = baseUrl
                val api = ApiClient.getApiService(baseUrl)
                val response = api.pairDevice(
                    PairRequest(
                        pairingCode = cleanCode,
                        deviceName = prefs.deviceName,
                        model = Build.MODEL,
                        androidVersion = Build.VERSION.SDK_INT
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    prefs.deviceId = body.deviceId
                    secretManager.saveDeviceToken(body.deviceToken)
                    body.allowedSenders?.let { prefs.allowedSenders = it.toSet() }
                    prefs.heartbeatSeconds = body.heartbeatSeconds ?: 120L
                    body.wallets?.let { prefs.saveWallets(it) }
                    prefs.isPaired = true
                    prefs.isAuthRevoked = false

                    PayGateForwarderService.start(getApplication())
                    _pairingState.value = PairingUiState.Success("Paired successfully with ID: ${body.deviceId}")
                } else {
                    val msg = response.errorBody()?.string() ?: "Pairing rejected by server (${response.code()})"
                    _pairingState.value = PairingUiState.Error(msg)
                }
            } catch (e: Exception) {
                _pairingState.value = PairingUiState.Error(e.localizedMessage ?: "Network error during pairing")
            }
        }
    }

    fun dismissIgnoredWarning() {
        prefs.hasIgnoredWarning = false
    }

    fun unpairDevice() {
        viewModelScope.launch {
            secretManager.clearSecrets()
            prefs.resetPairing()
            _pairingState.value = PairingUiState.Idle
            _actionFeedback.value = "Device unpaired."
        }
    }
}
