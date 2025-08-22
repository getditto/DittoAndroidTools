package live.ditto.dittotoolsapp.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import live.ditto.Ditto
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.service.AuthService
import live.ditto.dittotoolsapp.service.CredentialsService
import live.ditto.dittotoolsapp.service.DittoService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CredentialsViewModel @Inject constructor(
    application: Application,
    private val authService: AuthService,
    private val credentialsService: CredentialsService,
    private val dittoService: DittoService
) : AndroidViewModel(application) {

    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Idle)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState

    // Expose auth states from injected AuthService which observes DittoService
    val authStatus = authService.authStatus
    val isAuthenticated = authService.isAuthenticated
    val lastError = authService.lastError
    
    // Expose credentials state
    val activeCredentials: CredentialsData?
        get() = credentialsService.activeCredentials
    
    init {
        // Observe authentication state changes from AuthService
        viewModelScope.launch {
            authService.isAuthenticated.collect { authenticated ->
                // Update internal authentication state based on AuthService's observation
                if (authenticated && _authenticationState.value is AuthenticationState.Loading) {
                    _authenticationState.value = AuthenticationState.Success
                }
            }
        }
    }

    fun testConnection(credentialsData: CredentialsData, ditto: Ditto?) {
        viewModelScope.launch {
            _authenticationState.value = AuthenticationState.Loading
            // Clear any previous errors
            authService.clearError()
            
            var cleanup: (() -> Unit)? = null
            try {
                // Create DittoService for testing
                val testDittoService = DittoService(getApplication())
                
                // Set up temporary observation of the test service
                cleanup = authService.observeForTest(testDittoService)
                
                // Initialize the test Ditto instance
                testDittoService.initialize(credentialsData)
                
                // Wait for authentication status with timeout
                val authResult = withTimeout(5000) {
                    // Wait for auth status to be determined
                    var attempts = 0
                    while (attempts < 50) { // 50 attempts * 100ms = 5 seconds max
                        val status = authService.authStatus.value
                        when {
                            status?.isAuthenticated == true -> return@withTimeout true
                            status != null && !status.isAuthenticated -> return@withTimeout false
                        }
                        delay(100)
                        attempts++
                    }
                    false // Timeout reached
                }
                
                if (authResult) {
                    _authenticationState.value = AuthenticationState.Success
                    saveCredentials(credentialsData)
                } else {
                    val error = authService.lastError.value
                    _authenticationState.value = AuthenticationState.Failure(
                        error ?: "Authentication failed - invalid credentials"
                    )
                }
                
                // Clean up test instance
                testDittoService.stopSync()
                
            } catch (e: Exception) {
                authService.setError(e.message)
                _authenticationState.value = AuthenticationState.Failure(e.message ?: "Connection failed")
            } finally {
                // Restore previous observation
                cleanup?.invoke()
            }
        }
    }


    fun saveCredentials(credentialsData: CredentialsData) {
        credentialsService.activeCredentials = credentialsData
        authService.saveCredentials(credentialsData)
    }
    
    fun clearCredentials() {
        credentialsService.clearCredentials()
    }

    fun resetAuthenticationState() {
        _authenticationState.value = AuthenticationState.Idle
        authService.clearError()
    }
}

// Legacy compatibility
sealed class AuthenticationState {
    object Idle : AuthenticationState()
    object Loading : AuthenticationState()
    object Success : AuthenticationState()
    data class Failure(val message: String) : AuthenticationState()
}
