package com.ditto.dittotoolsapp

import android.app.Application
import android.util.Log
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoAuthenticationProvider
import com.ditto.kotlin.DittoConfig
import com.ditto.kotlin.DittoFactory
import com.ditto.kotlin.DittoLogLevel
import com.ditto.kotlin.DittoLogger
import com.ditto.kotlin.transports.DittoSyncPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DittoToolsApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var ditto: Ditto

    override fun onCreate() {
        super.onCreate()

        // Initialize Ditto in the background
        applicationScope.launch {
            try {
                DittoLogger.minimumLogLevel = DittoLogLevel.Debug

                val appId = requireEnv("DITTO_APP_ID", BuildConfig.DITTO_APP_ID)
                val playgroundToken = requireEnv("DITTO_PLAYGROUND_TOKEN", BuildConfig.DITTO_PLAYGROUND_TOKEN)
                val authUrl = requireEnv("DITTO_AUTH_URL", BuildConfig.DITTO_AUTH_URL)

                ditto = DittoFactory.create(
                    DittoConfig(
                        databaseId = appId,
                        connect = DittoConfig.Connect.Server(authUrl),
                    )
                ).apply {
                    auth?.expirationHandler = { dittoInstance, _ ->
                        dittoInstance.auth?.login(
                            token = playgroundToken,
                            provider = DittoAuthenticationProvider.development(),
                        )
                    }
                    sync.start()
                }

                Log.d(TAG, "Ditto initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Ditto", e)
            }
        }
    }

    private fun requireEnv(key: String, value: String): String {
        if (value.isEmpty()) {
            throw IllegalStateException(
                "$key not found. Copy DittoAndroidTools/.env.sample to DittoAndroidTools/.env " +
                    "and fill in values, or set $key as an environment variable before building."
            )
        }
        return value
    }

    fun getDittoOrNull(): Ditto? {
        return if (::ditto.isInitialized) ditto else null
    }

    fun missingPermissions(): Array<String> {
        return DittoSyncPermissions(applicationContext).missingPermissions()
    }

    companion object {
        private const val TAG = "DittoToolsApplication"
    }
}
