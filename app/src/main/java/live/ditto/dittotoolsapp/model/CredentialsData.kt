package live.ditto.dittotoolsapp.model

/**
 * Simple data class for storing credential information.
 * This is used for persistence and doesn't contain any Android-specific objects.
 */
data class CredentialsData(
    val identityType: IdentityType,
    val appId: String? = null,
    val token: String? = null,
    val enableCloudSync: Boolean = true,
    val siteId: Long? = null,
    val sharedKey: String? = null,
    val authProvider: String? = null,
    val authToken: String? = null,
    val offlineLicenseToken: String? = null,
    val socketUrl: String? = null
)