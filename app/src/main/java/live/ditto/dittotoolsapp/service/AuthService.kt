package live.ditto.dittotoolsapp.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.ditto.DittoAuthenticationStatus
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.service.CredentialsService
import live.ditto.dittotoolsapp.service.DittoService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that manages authentication and credentials state
 */
@Singleton
class AuthService @Inject constructor(
    private val credentialsService: CredentialsService
) {
    
    companion object {
        private const val TAG = "AuthService"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _authStatus = MutableStateFlow<DittoAuthenticationStatus?>(null)
    val authStatus: StateFlow<DittoAuthenticationStatus?> = _authStatus.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    private val _currentCredentials = MutableStateFlow<CredentialsData?>(null)
    val currentCredentials: StateFlow<CredentialsData?> = _currentCredentials.asStateFlow()
    
    private var dittoService: DittoService? = null
    
    /**
     * Initialize the auth service with a DittoService to observe
     */
    fun initialize(service: DittoService) {
        Log.d(TAG, "initialize: Setting up auth observation")
        // If already observing a service, stop the previous observation
        if (dittoService != null && dittoService != service) {
            Log.d(TAG, "initialize: Stopping previous observation")
            // Clear previous state but don't cancel the scope
            _authStatus.value = null
            _isAuthenticated.value = false
        }
        dittoService = service
        observeAuthStatus()
    }
    
    /**
     * Start observing authentication status from DittoService
     */
    private fun observeAuthStatus() {
        dittoService?.let { service ->
            scope.launch {
                try {
                    service.authStatus.collect { status ->
                        Log.d(TAG, "observeAuthStatus: Auth status changed to $status")
                        _authStatus.value = status
                        _isAuthenticated.value = status?.isAuthenticated == true
                        
                        // Clear error on successful authentication
                        if (status?.isAuthenticated == true) {
                            _lastError.value = null
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "observeAuthStatus: Error observing auth status", e)
                }
            }
        }
    }
    
    /**
     * Get active credentials from CredentialsService
     */
    fun getActiveCredentials(): CredentialsData? {
        Log.d(TAG, "getActiveCredentials: Retrieving active credentials")
        val credentials = credentialsService.activeCredentials
        _currentCredentials.value = credentials
        return credentials
    }
    
    /**
     * Save credentials
     */
    fun saveCredentials(credentials: CredentialsData) {
        Log.d(TAG, "saveCredentials: Saving credentials for identity type = ${credentials.identityType}")
        credentialsService.activeCredentials = credentials
        _currentCredentials.value = credentials
        _lastError.value = null
    }
    
    /**
     * Set error state
     */
    fun setError(error: String?) {
        Log.e(TAG, "setError: error = $error")
        _lastError.value = error
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        Log.d(TAG, "clearError: Clearing error state")
        _lastError.value = null
    }
    
    /**
     * Clear all state
     */
    fun clearState() {
        Log.d(TAG, "clearState: Clearing all state")
        _authStatus.value = null
        _isAuthenticated.value = false
        _lastError.value = null
        _currentCredentials.value = null
    }
    
    /**
     * Check if we have valid credentials
     */
    fun hasValidCredentials(): Boolean {
        val hasCredentials = getActiveCredentials() != null
        Log.d(TAG, "hasValidCredentials: $hasCredentials")
        return hasCredentials
    }
    
    /**
     * Temporarily observe a DittoService for testing
     * Returns a function to stop observing
     */
    fun observeForTest(service: DittoService): () -> Unit {
        Log.d(TAG, "observeForTest: Setting up temporary observation")
        val previousService = dittoService
        initialize(service)
        
        // Return cleanup function
        return {
            Log.d(TAG, "observeForTest: Cleaning up temporary observation")
            if (previousService != null) {
                initialize(previousService)
            } else {
                dittoService = null
                clearState()
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        Log.d(TAG, "destroy: Cleaning up resources")
        scope.cancel()
        dittoService = null
        clearState()
    }
}