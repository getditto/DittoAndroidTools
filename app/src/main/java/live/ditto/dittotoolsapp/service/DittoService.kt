package live.ditto.dittotoolsapp.service

import android.content.Context
import android.util.Log
import live.ditto.Ditto
import live.ditto.DittoAuthenticationCallback
import live.ditto.DittoAuthenticator
import live.ditto.DittoIdentity
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.model.IdentityType
import java.math.BigInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import live.ditto.DittoError
import live.ditto.DittoAuthenticationStatusDidChangeCallback
import live.ditto.DittoAuthenticationStatus
import live.ditto.DittoPresenceGraph
import live.ditto.DittoPresenceObserver
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DittoService @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val TAG = "DittoService"
    }

    lateinit var ditto: Ditto
        private set
    
    private var authStatusObserver: DittoAuthenticationStatusDidChangeCallback? = null
    private var presenceObserver: DittoPresenceObserver? = null
    
    private val _authStatus = MutableStateFlow<DittoAuthenticationStatus?>(null)
    val authStatus: StateFlow<DittoAuthenticationStatus?> = _authStatus.asStateFlow()
    
    private val _presenceGraph = MutableStateFlow<DittoPresenceGraph?>(null)
    val presenceGraph: StateFlow<DittoPresenceGraph?> = _presenceGraph.asStateFlow()

    fun initialize(credentialsData: CredentialsData) {
        Log.d(TAG, "initialize: Starting initialization with identity type = ${credentialsData.identityType}")
        val androidDependencies = DefaultAndroidDittoDependencies(context)
        val identity = createIdentity(credentialsData)
        ditto = Ditto(androidDependencies, identity)
        
        // Set log level
        DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG
        
        ditto.startSync()
        Log.d(TAG, "initialize: Ditto initialized and sync started")
        
        // Start observing status
        startObserving()
    }

    private fun createIdentity(credentialsData: CredentialsData): DittoIdentity {
        Log.d(TAG, "createIdentity: Creating identity for type = ${credentialsData.identityType}")
        val androidDependencies = DefaultAndroidDittoDependencies(context)
        return when (credentialsData.identityType) {
            IdentityType.ONLINE_PLAYGROUND -> {
                Log.d(TAG, "createIdentity: Creating OnlinePlayground identity with appId = ${credentialsData.appId}")
                DittoIdentity.OnlinePlayground(
                    androidDependencies,
                    appId = credentialsData.appId ?: throw IllegalArgumentException("App ID required"),
                    token = credentialsData.token ?: throw IllegalArgumentException("Token required"),
                    enableDittoCloudSync = credentialsData.enableCloudSync
                )
            }
            IdentityType.OFFLINE_PLAYGROUND -> {
                Log.d(TAG, "createIdentity: Creating OfflinePlayground identity with appId = ${credentialsData.appId}, siteId = ${credentialsData.siteId}")
                DittoIdentity.OfflinePlayground(
                    androidDependencies,
                    appId = credentialsData.appId ?: throw IllegalArgumentException("App ID required"),
                    siteId = credentialsData.siteId?.let { BigInteger.valueOf(it) } ?: BigInteger.valueOf(1)
                )
            }
            IdentityType.SHARED_KEY -> {
                Log.d(TAG, "createIdentity: Creating SharedKey identity with appId = ${credentialsData.appId}, siteId = ${credentialsData.siteId}")
                DittoIdentity.SharedKey(
                    androidDependencies,
                    appId = credentialsData.appId ?: throw IllegalArgumentException("App ID required"),
                    sharedKey = credentialsData.sharedKey ?: throw IllegalArgumentException("Shared key required"),
                    siteId = credentialsData.siteId?.let { BigInteger.valueOf(it) } ?: BigInteger.valueOf(1)
                )
            }
            IdentityType.ONLINE_WITH_AUTHENTICATION -> {
                Log.d(TAG, "createIdentity: Creating OnlineWithAuthentication identity with appId = ${credentialsData.appId}, authProvider = ${credentialsData.authProvider}")
                DittoIdentity.OnlineWithAuthentication(
                    androidDependencies,
                    appId = credentialsData.appId ?: throw IllegalArgumentException("App ID required"),
                    customAuthUrl = credentialsData.authProvider,
                    enableDittoCloudSync = credentialsData.enableCloudSync,
                    callback = object : DittoAuthenticationCallback {
                        override fun authenticationRequired(authenticator: DittoAuthenticator) {
                            Log.d(TAG, "authenticationRequired: Authentication required for OnlineWithAuthentication")
                            val token = credentialsData.token ?: ""
                            val authProvider = credentialsData.authProvider ?: ""
                            authenticator.login(token, authProvider) { userId: String?, error: DittoError? ->
                                if (error != null) {
                                    Log.e(TAG, "authenticationRequired: Authentication failed - ${error.message}")
                                }
                            }
                        }

                        override fun authenticationExpiringSoon(
                            authenticator: DittoAuthenticator,
                            secondsRemaining: Long
                        ) {
                            Log.d(TAG, "authenticationExpiringSoon: Auth token expiring in $secondsRemaining seconds")
                        }
                    }
                )
            }
            IdentityType.MANUAL -> {
                Log.d(TAG, "createIdentity: Creating Manual identity")
                DittoIdentity.Manual("manual")
            }
        }
    }

    fun observeAuthStatus(): Flow<DittoAuthenticationStatus> = callbackFlow {
        val callback = object : DittoAuthenticationStatusDidChangeCallback {
            override fun authenticationStatusDidChange(status: DittoAuthenticationStatus) {
                Log.d(TAG, "observeAuthStatus: Auth status changed to $status")
                trySend(status)
            }
        }

        Log.d(TAG, "observeAuthStatus: Setting up auth status observer")
        ditto.auth?.observeStatus(callback)

        awaitClose {
            // TODO: Remove auth status observer when API is available
            // ditto.auth?.removeObserver(callback)
        }
    }

    fun observePresenceGraph(): Flow<DittoPresenceGraph> = callbackFlow {
        Log.d(TAG, "observePresenceGraph: Setting up presence graph observer")
        val observer = ditto.presence.observe {
            Log.d(TAG, "observePresenceGraph: Presence graph updated, peer count = ${it.remotePeers.size}")
            trySend(it)
        }

        awaitClose {
            observer.close()
        }
    }

    /**
     * Create a Ditto instance for testing purposes
     */
    fun createTestDittoInstance(credentialsData: CredentialsData): Ditto {
        Log.d(TAG, "createTestDittoInstance: Creating test instance for identity type = ${credentialsData.identityType}")
        return Ditto(
            DefaultAndroidDittoDependencies(context),
            createIdentity(credentialsData)
        )
    }

    suspend fun testAuthentication(credentialsData: CredentialsData): Boolean {
        Log.d(TAG, "testAuthentication: Testing authentication for identity type = ${credentialsData.identityType}")
        val testDitto = createTestDittoInstance(credentialsData)
        return try {
            testDitto.startSync()
            Log.d(TAG, "testAuthentication: Test Ditto started sync")
            // For OnlineWithAuthentication, we need to wait for the auth callback
            if (credentialsData.identityType == IdentityType.ONLINE_WITH_AUTHENTICATION) {
                Log.d(TAG, "testAuthentication: Testing OnlineWithAuthentication, waiting for auth callback")
                val authResult = CompletableDeferred<Boolean>()
                val authCallback = object : DittoAuthenticationCallback {
                    override fun authenticationRequired(authenticator: DittoAuthenticator) {
                        Log.d(TAG, "testAuthentication: Authentication required callback triggered")
                        val token = credentialsData.token ?: ""
                        val authProvider = credentialsData.authProvider ?: ""
                        authenticator.login(token, authProvider) { userId: String?, error: DittoError? ->
                            if (error == null) {
                                Log.d(TAG, "testAuthentication: Authentication successful, userId = $userId")
                                authResult.complete(true)
                            } else {
                                Log.e(TAG, "testAuthentication: Authentication failed, error = ${error.message}")
                                authResult.complete(false)
                            }
                        }
                    }

                    override fun authenticationExpiringSoon(
                        authenticator: DittoAuthenticator,
                        secondsRemaining: Long
                    ) {
                        Log.d(TAG, "testAuthentication: Auth token expiring in $secondsRemaining seconds")
                    }
                }
                // Auth callback is handled via identity callback
                // testDitto.auth?.setAuthenticationCallback(authCallback)
                val result = authResult.await()
                testDitto.stopSync()
                Log.d(TAG, "testAuthentication: OnlineWithAuthentication test result = $result")
                result
            } else {
                testDitto.stopSync()
                Log.d(TAG, "testAuthentication: Test successful for ${credentialsData.identityType}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "testAuthentication: Test failed with exception", e)
            testDitto.stopSync()
            false
        }
    }
    
    /**
     * Check if Ditto is initialized
     */
    fun isInitialized(): Boolean {
        return ::ditto.isInitialized
    }
    
    /**
     * Stop Ditto sync
     */
    fun stopSync() {
        if (isInitialized()) {
            Log.d(TAG, "stopSync: Stopping Ditto sync")
            stopObserving()
            ditto.stopSync()
        }
    }
    
    /**
     * Start observing Ditto's authentication status and presence
     */
    private fun startObserving() {
        Log.d(TAG, "startObserving: Starting to observe Ditto status")
        
        // Get initial auth status
        _authStatus.value = ditto.auth?.status
        Log.d(TAG, "startObserving: Initial auth status = ${ditto.auth?.status}")
        
        // Set up auth status observer
        authStatusObserver = object : DittoAuthenticationStatusDidChangeCallback {
            override fun authenticationStatusDidChange(status: DittoAuthenticationStatus) {
                Log.d(TAG, "authenticationStatusDidChange: Status changed to $status")
                _authStatus.value = status
            }
        }
        
        ditto.auth?.observeStatus(authStatusObserver!!)
        Log.d(TAG, "startObserving: Auth observer set up")
        
        // Set up presence observer
        presenceObserver = ditto.presence.observe { graph ->
            Log.d(TAG, "presenceObserver: Presence graph updated, peer count = ${graph.remotePeers.size}")
            _presenceGraph.value = graph
        }
        Log.d(TAG, "startObserving: Presence observer set up")
    }
    
    /**
     * Stop observing Ditto status
     */
    private fun stopObserving() {
        Log.d(TAG, "stopObserving: Stopping all observers")
        // TODO: Remove auth observer when API is available
        // authStatusObserver?.let { ditto.auth?.removeObserver(it) }
        authStatusObserver = null
        
        presenceObserver?.close()
        presenceObserver = null
        
        _authStatus.value = null
        _presenceGraph.value = null
    }
}
