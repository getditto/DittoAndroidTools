package live.ditto.dittotoolsapp.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.model.IdentityType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the active credentials for the application.
 *
 * `CredentialsService` acts as the single source of truth for managing user or app credentials.
 * It provides a single public interface, `activeCredentials`, which allows:
 * - Retrieving credentials from memory or encrypted SharedPreferences.
 * - Setting or removing credentials and ensuring they are securely persisted.
 *
 * The service ensures that credentials are securely managed without exposing internal details of
 * encryption operations or authentication handling.
 *
 * ### Key Features
 * - Retrieve credentials from memory or encrypted SharedPreferences transparently.
 * - Save or remove credentials securely in encrypted SharedPreferences.
 * - Encapsulation: Only `activeCredentials` is exposed to external components; all other operations are internal.
 *
 * Example Usage:
 * ```kotlin
 * if (credentialsService.activeCredentials != null) {
 *     println("Loaded credentials: ${credentialsService.activeCredentials}")
 * } else {
 *     println("No active credentials found.")
 * }
 * ```
 *
 * - Note: Setting `activeCredentials` to `null` will clear the encrypted storage.
 */
@Singleton
class CredentialsService @Inject constructor(@ApplicationContext applicationContext: Context) {
    
    private companion object {
        const val PREFS_NAME = "ditto_credentials_prefs"
        const val KEY_IDENTITY_TYPE = "identity_type"
        const val KEY_APP_ID = "app_id"
        const val KEY_TOKEN = "token"
        const val KEY_ENABLE_CLOUD_SYNC = "enable_cloud_sync"
        const val KEY_SITE_ID = "site_id"
        const val KEY_SHARED_KEY = "shared_key"
        const val KEY_AUTH_PROVIDER = "auth_provider"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_OFFLINE_LICENSE_TOKEN = "offline_license_token"
        const val KEY_SOCKET_URL = "socket_url"
    }
    
    private val encryptedPrefs: SharedPreferences
    private var storedCredentials: CredentialsData? = null

    init {
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * The active credentials used by the app.
     * Note: Only the credential data is returned, not a full DittoIdentity object.
     * The caller must recreate the DittoIdentity with their own Context.
     */
    var activeCredentials: CredentialsData?
        get() {
            // Return the cached credentials if already set
            storedCredentials?.let { return it }
            
            // Attempt to load the credentials from encrypted SharedPreferences if not cached
            loadCredentialsFromStorage()?.let { loadedCredentials ->
                storedCredentials = loadedCredentials // Cache it for future access
                return loadedCredentials
            }
            
            // Return null if no credentials are found in storage
            return null
        }
        set(newValue) {
            // Cache the new credentials in memory
            storedCredentials = newValue
            
            // Save the new credentials to encrypted SharedPreferences, or remove them if null
            if (newValue != null) {
                saveCredentialsToStorage(newValue)
                println("CredentialsService added credentials!")
            } else {
                removeCredentialsFromStorage()
                println("CredentialsService removed credentials!")
            }
        }

    fun clearCredentials() {
        activeCredentials = null
    }
    
    // MARK: - Encrypted SharedPreferences Integration
    
    /**
     * Saves the provided credentials to encrypted SharedPreferences.
     */
    private fun saveCredentialsToStorage(credentials: CredentialsData) {
        try {
            val editor = encryptedPrefs.edit()
            
            // Save identity type
            editor.putString(KEY_IDENTITY_TYPE, credentials.identityType.name)
            
            // Save credential fields
            credentials.appId?.let { editor.putString(KEY_APP_ID, it) }
            credentials.token?.let { editor.putString(KEY_TOKEN, it) }
            editor.putBoolean(KEY_ENABLE_CLOUD_SYNC, credentials.enableCloudSync)
            credentials.siteId?.let { editor.putLong(KEY_SITE_ID, it) }
            credentials.sharedKey?.let { editor.putString(KEY_SHARED_KEY, it) }
            credentials.authProvider?.let { editor.putString(KEY_AUTH_PROVIDER, it) }
            credentials.authToken?.let { editor.putString(KEY_AUTH_TOKEN, it) }
            credentials.offlineLicenseToken?.let { editor.putString(KEY_OFFLINE_LICENSE_TOKEN, it) }
            credentials.socketUrl?.let { editor.putString(KEY_SOCKET_URL, it) }
            
            editor.apply()
            println("Saved credentials to encrypted storage!")
        } catch (e: Exception) {
            println("Failed to save credentials: ${e.message}")
        }
    }
    
    /**
     * Removes credentials from encrypted SharedPreferences.
     */
    private fun removeCredentialsFromStorage() {
        try {
            encryptedPrefs.edit().clear().apply()
            println("Credentials removed from encrypted storage.")
        } catch (e: Exception) {
            println("Failed to remove credentials: ${e.message}")
        }
    }
    
    /**
     * Loads credentials from encrypted SharedPreferences.
     */
    private fun loadCredentialsFromStorage(): CredentialsData? {
        return try {
            val identityTypeString = encryptedPrefs.getString(KEY_IDENTITY_TYPE, null) ?: return null
            val identityType = IdentityType.valueOf(identityTypeString)
            
            CredentialsData(
                identityType = identityType,
                appId = encryptedPrefs.getString(KEY_APP_ID, null),
                token = encryptedPrefs.getString(KEY_TOKEN, null),
                enableCloudSync = encryptedPrefs.getBoolean(KEY_ENABLE_CLOUD_SYNC, true),
                siteId = if (encryptedPrefs.contains(KEY_SITE_ID)) encryptedPrefs.getLong(KEY_SITE_ID, 1) else null,
                sharedKey = encryptedPrefs.getString(KEY_SHARED_KEY, null),
                authProvider = encryptedPrefs.getString(KEY_AUTH_PROVIDER, null),
                authToken = encryptedPrefs.getString(KEY_AUTH_TOKEN, null),
                offlineLicenseToken = encryptedPrefs.getString(KEY_OFFLINE_LICENSE_TOKEN, null),
                socketUrl = encryptedPrefs.getString(KEY_SOCKET_URL, null)
            )
        } catch (e: Exception) {
            println("Failed to load credentials: ${e.message}")
            null
        }
    }
}