package com.paygate.smsfwd

import android.app.Application
import com.paygate.smsfwd.data.local.db.AppDatabase
import com.paygate.smsfwd.data.prefs.PreferenceManager
import com.paygate.smsfwd.data.security.KeystoreSecretManager
import com.paygate.smsfwd.service.PayGateForwarderService

class PayGateApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var prefs: PreferenceManager
        private set

    lateinit var secretManager: KeystoreSecretManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        prefs = PreferenceManager(this)
        secretManager = KeystoreSecretManager(this)

        if (secretManager.getDeviceToken().isNullOrBlank()) {
            secretManager.saveDeviceToken(PreferenceManager.DEFAULT_DEVICE_TOKEN)
            prefs.apiBaseUrl = PreferenceManager.DEFAULT_BASE_URL
            prefs.isPaired = true
        }

        if (prefs.isPaired && prefs.isServiceEnabled && !prefs.isAuthRevoked) {
            PayGateForwarderService.start(this)
        }
    }
}
