package com.paygate.smsfwd.service

import android.content.Context
import android.util.Log
import com.paygate.smsfwd.data.local.db.AppDatabase
import com.paygate.smsfwd.data.local.entity.QueuedSms
import com.paygate.smsfwd.data.prefs.PreferenceManager
import com.paygate.smsfwd.data.remote.ApiClient
import com.paygate.smsfwd.data.remote.PayGateApiService
import com.paygate.smsfwd.data.remote.model.WebhookRequest
import com.paygate.smsfwd.data.security.KeystoreSecretManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * QueueSyncManager handles the robust FIFO delivery of queued SMS transactions
 * adhering to PRD FR-7, FR-8, FR-9, FR-10, FR-11, FR-14, and FR-15.
 */
object QueueSyncManager {

    private const val TAG = "QueueSyncManager"
    private val drainMutex = Mutex()

    suspend fun drainQueue(context: Context): Int = withContext(Dispatchers.IO) {
        drainMutex.withLock {
            val prefs = PreferenceManager(context.applicationContext)
            if (!prefs.isPaired || prefs.isAuthRevoked || prefs.isMaintenanceMode) {
                return@withLock 0
            }

            val db = AppDatabase.getInstance(context.applicationContext)
            val dao = db.queuedSmsDao()
            val secretManager = KeystoreSecretManager(context.applicationContext)

            // Mark items older than 24h as STALE (they remain in queue and continue retrying per FR-10)
            val staleCutoff = System.currentTimeMillis() - 86_400_000L
            dao.markStaleItems(staleCutoff)

            val deliverable = dao.getDeliverableSms(
                statuses = listOf(QueuedSms.STATUS_PENDING, QueuedSms.STATUS_STALE),
                limit = 25
            )

            if (deliverable.isEmpty()) {
                return@withLock 0
            }

            val apiService = ApiClient.getApiService(prefs.apiBaseUrl)
            val rawToken = secretManager.getDeviceToken() ?: "mock_bearer_token"
            val bearerHeader = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

            var deliveredCount = 0

            for (sms in deliverable) {
                // FR-15: Block forwarding if SIM slot is not mapped to an active registered wallet
                if (!prefs.isSlotMapped(sms.simSlot)) {
                    Log.w(TAG, "SIM slot ${sms.simSlot} not registered in wallet registry. Pausing dispatch.")
                    continue
                }

                val now = System.currentTimeMillis()
                val request = WebhookRequest(
                    payloadVersion = 3,
                    sender = sms.sender,
                    body = sms.body,
                    clientMsgId = sms.clientMsgId,
                    deviceSeq = sms.deviceSeq,
                    receivedAt = sms.receivedAt / 1000L, // server expects seconds
                    simSlot = sms.simSlot,
                    refHint = sms.refHint,
                    parserVersion = "app-3.0.0",
                    source = sms.source,
                    appVersion = "3.0.0"
                )

                try {
                    val response = apiService.postSms(
                        bearerToken = bearerHeader,
                        request = request
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        val latency = now - sms.receivedAt
                        val ignoredNote = if (body?.ignored == true) "Server ignored: ${body.reason ?: "unknown"}" else null
                        val updated = sms.copy(
                            status = QueuedSms.STATUS_DELIVERED,
                            deliveredAt = now,
                            serverAckTrxId = body?.trxId ?: sms.trxId,
                            deliveryLatencyMs = latency,
                            errorMessage = ignoredNote
                        )
                        dao.update(updated)
                        if (ignoredNote != null) {
                            prefs.hasIgnoredWarning = true
                        }
                        deliveredCount++

                        // Track reference telemetry
                        if (!sms.refHint.isNullOrBlank()) {
                            prefs.refSeenCount += 1
                        } else {
                            prefs.noRefCount += 1
                        }
                    } else {
                        val code = response.code()
                        if (code == 401 || code == 403) {
                            // FR-14: Pause forwarding, keep queue, mark auth revoked
                            Log.e(TAG, "HTTP $code Auth Revoked from server. Halting queue.")
                            prefs.isAuthRevoked = true
                            val updated = sms.copy(
                                status = QueuedSms.STATUS_AUTH_PAUSED,
                                errorMessage = "HTTP $code Auth Revoked / Token Expired"
                            )
                            dao.update(updated)
                            break
                        } else if (code == 400 || code == 413 || code == 422) {
                            dao.update(sms.copy(
                                attemptCount = sms.attemptCount + 1,
                                lastAttemptAt = now,
                                status = QueuedSms.STATUS_FAILED,
                                errorMessage = "HTTP $code (not retryable)"
                            ))
                        } else {
                            val attempts = sms.attemptCount + 1
                            val nextStatus = if (attempts >= 20) QueuedSms.STATUS_FAILED else sms.status
                            val updated = sms.copy(
                                attemptCount = attempts,
                                lastAttemptAt = now,
                                status = nextStatus,
                                errorMessage = "HTTP $code: ${response.message()}"
                            )
                            dao.update(updated)
                            // Short delay between retries
                            delay(500)
                        }
                    }
                } catch (e: Exception) {
                    val attempts = sms.attemptCount + 1
                    val updated = sms.copy(
                        attemptCount = attempts,
                        lastAttemptAt = now,
                        errorMessage = e.localizedMessage ?: "Network error"
                    )
                    dao.update(updated)
                    Log.e(TAG, "Failed delivery attempt for ID ${sms.id}: ${e.message}")
                    break // Stop draining if network drops
                }
            }

            prefs.lastSyncAt = System.currentTimeMillis()
            return@withLock deliveredCount
        }
    }
}
