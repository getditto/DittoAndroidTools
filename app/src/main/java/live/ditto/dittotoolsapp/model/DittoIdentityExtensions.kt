package live.ditto.dittotoolsapp.model

import live.ditto.DittoIdentity

/**
 * Extension to `DittoIdentity` for extracting associated values and managing identity types.
 */

/**
 * Retrieves the `appID` associated with the `DittoIdentity` instance.
 *
 * This computed property returns the `appID` value for identity types that include it
 * (e.g., `offlinePlayground`, `onlineWithAuthentication`, etc.). If the identity type
 * does not have an `appID` (e.g., `manual`), it returns `null`.
 *
 * @return The `appID` if available, or `null` for identity types that do not have one.
 */
val DittoIdentity.appID: String?
    get() = when (this) {
        is DittoIdentity.OfflinePlayground -> this.appId
        is DittoIdentity.OnlinePlayground -> this.appId
        is DittoIdentity.OnlineWithAuthentication -> this.appId
        is DittoIdentity.SharedKey -> this.appId
        is DittoIdentity.Manual -> null
    }

/**
 * Enum representing the different identity types of `DittoIdentity`.
 *
 * This enum simplifies working with identity types by removing the associated values
 * present in `DittoIdentity`. It enables iteration over all identity types.
 */
enum class IdentityType(val displayName: String) {
    OFFLINE_PLAYGROUND("Offline Playground"),
    ONLINE_WITH_AUTHENTICATION("Online with Authentication"),
    ONLINE_PLAYGROUND("Online Playground"),
    SHARED_KEY("Shared Key"),
    MANUAL("Manual");

    companion object {
        /**
         * All possible identity types.
         */
        val allCases = values().toList()
    }
}

/**
 * Computed property to derive the `IdentityType` from a `DittoIdentity` instance.
 *
 * This property maps the current `DittoIdentity` case to its corresponding `IdentityType`.
 * This allows you to work with the identity type in a simpler, associated-value-free format.
 *
 * @return The corresponding `IdentityType` for the current `DittoIdentity` instance.
 */
val DittoIdentity.identityType: IdentityType
    get() = when (this) {
        is DittoIdentity.OfflinePlayground -> IdentityType.OFFLINE_PLAYGROUND
        is DittoIdentity.OnlineWithAuthentication -> IdentityType.ONLINE_WITH_AUTHENTICATION
        is DittoIdentity.OnlinePlayground -> IdentityType.ONLINE_PLAYGROUND
        is DittoIdentity.SharedKey -> IdentityType.SHARED_KEY
        is DittoIdentity.Manual -> IdentityType.MANUAL
    }