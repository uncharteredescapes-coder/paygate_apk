package com.paygate.smsfwd

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.paygate.smsfwd.data.prefs.PreferenceManager
import com.paygate.smsfwd.data.remote.ApiClient
import com.paygate.smsfwd.data.remote.model.WebhookResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QueueAndClientConfigTest {

    @Test
    fun `mock mode is disabled by default`() {
        assertFalse(ApiClient.mockServerEnabled)
    }

    @Test
    fun `default base url and device id are production hardened`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager(context)

        assertEquals("https://paygator.vercel.app/api", PreferenceManager.DEFAULT_BASE_URL)
        assertEquals("https://paygator.vercel.app/api", prefs.apiBaseUrl)
        assertEquals("", prefs.deviceId)
        assertFalse(prefs.mockServerMode)
    }

    @Test
    fun `webhook response parses reason field correctly`() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(WebhookResponse::class.java)

        val json = """{"stored": true, "ignored": true, "reason": "unmapped_wallet_slot"}"""
        val response = adapter.fromJson(json)

        assertEquals(true, response?.stored)
        assertEquals(true, response?.ignored)
        assertEquals("unmapped_wallet_slot", response?.reason)
    }

    @Test
    fun `hasIgnoredWarning flags and resets properly in PreferenceManager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager(context)

        assertFalse(prefs.hasIgnoredWarning)
        prefs.hasIgnoredWarning = true
        assertTrue(prefs.hasIgnoredWarning)
        assertTrue(prefs.hasIgnoredWarningFlow.value)

        prefs.hasIgnoredWarning = false
        assertFalse(prefs.hasIgnoredWarning)
        assertFalse(prefs.hasIgnoredWarningFlow.value)
    }

    @Test
    fun `sanitizeBaseUrl normalizes urls, removes trailing slashes, and enforces https and api path`() {
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("https://paygator.vercel.app"))
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("https://paygator.vercel.app/"))
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("https://paygator.vercel.app/api"))
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("https://paygator.vercel.app/api/"))
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("http://paygator.vercel.app"))
        assertEquals("https://paygator.vercel.app/api", ApiClient.sanitizeBaseUrl("paygator.vercel.app"))
    }

    @Test
    fun `default device token matches backend provisioned token`() {
        assertEquals("9440e417a588b8efea01bdf896522560fe9c9575d386e224ccc64a2f7442e95e", PreferenceManager.DEFAULT_DEVICE_TOKEN)
    }
}
