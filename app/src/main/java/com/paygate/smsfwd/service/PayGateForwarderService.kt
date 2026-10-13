package com.paygate.smsfwd.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.paygate.smsfwd.MainActivity
import com.paygate.smsfwd.R
import com.paygate.smsfwd.data.local.db.AppDatabase
import com.paygate.smsfwd.data.prefs.PreferenceManager
import com.paygate.smsfwd.data.remote.ApiClient
import com.paygate.smsfwd.data.remote.model.HeartbeatRequest
import com.paygate.smsfwd.data.remote.model.SimSlotState
import com.paygate.smsfwd.data.security.KeystoreSecretManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PayGateForwarderService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: PreferenceManager
    private lateinit var db: AppDatabase
    private lateinit var secretManager: KeystoreSecretManager

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(applicationContext)
        db = AppDatabase.getInstance(applicationContext)
        secretManager = KeystoreSecretManager(applicationContext)

        createNotificationChannel()
        startForegroundServiceNotification("PayGate Forwarder running")

        // Start background polling and periodic 120s heartbeat loop
        startBackgroundLoops()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TRIGGER_DRAIN -> {
                serviceScope.launch {
                    QueueSyncManager.drainQueue(applicationContext)
                    updateNotification()
                }
            }
            ACTION_TRIGGER_HEARTBEAT -> {
                serviceScope.launch {
                    sendHeartbeat()
                }
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startBackgroundLoops() {
        // Periodic queue drain every 30 seconds
        serviceScope.launch {
            while (isActive) {
                if (prefs.isPaired && !prefs.isAuthRevoked && !prefs.isMaintenanceMode) {
                    QueueSyncManager.drainQueue(applicationContext)
                    updateNotification()
                }
                delay(30_000L)
            }
        }

        // Periodic 120s heartbeat loop (FR-21)
        serviceScope.launch {
            while (isActive) {
                if (prefs.isPaired && !prefs.isAuthRevoked) {
                    sendHeartbeat()
                }
                val intervalSeconds = prefs.heartbeatSeconds.coerceAtLeast(30L)
                delay(intervalSeconds * 1000L)
            }
        }
    }

    private suspend fun sendHeartbeat() {
        try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
            val isCharging = false

            val apiService = ApiClient.getApiService(prefs.apiBaseUrl)
            val rawToken = secretManager.getDeviceToken() ?: "mock_token"
            val bearer = if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken else "Bearer $rawToken"

            val request = HeartbeatRequest(
                battery = batteryLevel,
                charging = isCharging,
                network = "wifi_or_cellular",
                queueDepth = 0,
                staleCount = 0,
                failedCount = 0,
                permSms = true,
                batteryExempt = true,
                clockSkewMs = 0L,
                freeStorageMb = 4096L,
                simState = listOf(
                    SimSlotState(slot = 1, present = true),
                    SimSlotState(slot = 2, present = true)
                ),
                refSeenCount = prefs.refSeenCount,
                noRefCount = prefs.noRefCount,
                deviceSeq = prefs.deviceSeq
            )

            val resp = apiService.sendHeartbeat(bearer, request)
            if (resp.isSuccessful) {
                prefs.lastHeartbeatAt = System.currentTimeMillis()
                val body = resp.body()
                if (body != null) {
                    body.allowedSenders?.let { newSenders ->
                        if (newSenders.isNotEmpty()) {
                            prefs.allowedSenders = newSenders.toSet()
                        }
                    }
                    body.commands?.forEach { cmd ->
                        when (cmd.uppercase()) {
                            "FLUSH_QUEUE" -> QueueSyncManager.drainQueue(applicationContext)
                            "REVOKE_TOKEN" -> prefs.isAuthRevoked = true
                        }
                    }
                }
            } else if (resp.code() == 401 || resp.code() == 403) {
                prefs.isAuthRevoked = true
            }
        } catch (_: Exception) {
            // Transient network failure handled on next heartbeat cycle
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PayGate Forwarder Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground monitoring and SMS queue synchronization"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PayGate SMS Forwarder")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startForegroundServiceNotification(statusText: String) {
        val notification = buildNotification(statusText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, buildNotification("Monitoring active • SIM Wallets ready"))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "paygate_forwarder_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.paygate.smsfwd.START"
        const val ACTION_STOP = "com.paygate.smsfwd.STOP"
        const val ACTION_TRIGGER_DRAIN = "com.paygate.smsfwd.TRIGGER_DRAIN"
        const val ACTION_TRIGGER_HEARTBEAT = "com.paygate.smsfwd.TRIGGER_HEARTBEAT"

        fun start(context: Context) {
            val intent = Intent(context, PayGateForwarderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PayGateForwarderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun triggerDrain(context: Context) {
            val intent = Intent(context, PayGateForwarderService::class.java).apply {
                action = ACTION_TRIGGER_DRAIN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun triggerHeartbeat(context: Context) {
            val intent = Intent(context, PayGateForwarderService::class.java).apply {
                action = ACTION_TRIGGER_HEARTBEAT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
