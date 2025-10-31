package live.ditto.dittotoolsapp

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoIdentity
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.transports.DittoSyncPermissions

class DittoToolsApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var ditto: Ditto

    override fun onCreate() {
        super.onCreate()

        // Initialize Ditto in the background
        applicationScope.launch {
            try {
                DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG

                val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
                val identity = DittoIdentity.OnlinePlayground(
                    dependencies = androidDependencies,
                    appId = BuildConfig.DITTO_ONLINE_PLAYGROUND_APP_ID,
                    token = BuildConfig.DITTO_ONLINE_PLAYGROUND_TOKEN,
                    customAuthUrl = BuildConfig.DITTO_CUSTOM_AUTH_URL,
                    enableDittoCloudSync = false
                )

                ditto = Ditto(
                    dependencies = androidDependencies,
                    identity = identity
                ).apply {
                    disableSyncWithV3()

                    updateTransportConfig { transportConfig ->
                        transportConfig.connect.websocketUrls.add(BuildConfig.DITTO_WEBSOCKET_URL)
                    }

                    store.execute("ALTER SYSTEM SET DQL_STRICT_MODE = false")

                    startSync()
                }

                Log.d(TAG, "Ditto initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Ditto", e)
            }
        }
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
