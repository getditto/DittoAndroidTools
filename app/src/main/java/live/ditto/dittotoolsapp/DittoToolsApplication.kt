package live.ditto.dittotoolsapp

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

                val appId = BuildConfig.DITTO_ONLINE_PLAYGROUND_APP_ID
                ditto = DittoFactory.create(
                    DittoConfig(
                        databaseId = appId,
                        connect = DittoConfig.Connect.Server("https://$appId.cloud.ditto.live"),
                    )
                ).apply {
                    auth?.expirationHandler = { dittoInstance, _ ->
                        dittoInstance.auth?.login(
                            token = BuildConfig.DITTO_ONLINE_PLAYGROUND_TOKEN,
                            provider = DittoAuthenticationProvider.development(),
                        )
                    }
                    store.execute("ALTER SYSTEM SET DQL_STRICT_MODE = false")
                    sync.start()
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
