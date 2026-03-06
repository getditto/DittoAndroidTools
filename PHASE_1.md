# Phase 1: Research & Setup Results

## SDK Availability

- **v5 is on Maven Central**: `com.ditto:ditto-kotlin:5.0.0-preview.5`
- URL: https://central.sonatype.com/artifact/com.ditto/ditto-kotlin/5.0.0-preview.5
- **Group changed**: `live.ditto` -> `com.ditto`
- **Artifact name**: `ditto-kotlin` (was `ditto`)
- **Full coordinates**: `com.ditto:ditto-kotlin:5.0.0-preview.5`

## Package Name Changes

| v4 | v5 |
|---|---|
| `live.ditto.Ditto` | `com.ditto.kotlin.Ditto` |
| `live.ditto.DittoIdentity` | **Removed** — replaced with `com.ditto.kotlin.DittoConfig` |
| `live.ditto.android.DefaultAndroidDittoDependencies` | **Removed** — handled internally |
| `live.ditto.DittoPeer` | `com.ditto.kotlin.DittoPeer` |
| `live.ditto.DittoPresenceGraph` | `com.ditto.kotlin.DittoPresenceGraph` |
| `live.ditto.DittoConnectionType` | `com.ditto.kotlin.DittoConnectionType` |
| `live.ditto.DittoLogger` | `com.ditto.kotlin.DittoLogger` |
| `live.ditto.DittoLogLevel` | `com.ditto.kotlin.DittoLogLevel` |
| `live.ditto.DittoError` | `com.ditto.kotlin.DittoException` (sealed class) |
| `live.ditto.DittoCollection` | **Removed** — use DQL strings |
| `live.ditto.DittoDocument` | **Removed** — use `DittoQueryResultItem` |
| `live.ditto.DittoLiveQuery` | **Removed** — use `DittoStoreObserver` |
| `live.ditto.DittoLiveQueryEvent` | **Removed** |
| `live.ditto.DittoPendingCursorOperation` | **Removed** — use DQL |
| `live.ditto.DittoSubscription` | `com.ditto.kotlin.DittoSyncSubscription` |
| `live.ditto.DiskUsageItem` | `com.ditto.kotlin.DittoDiskUsageItem` |
| `live.ditto.Presence` | `com.ditto.kotlin.DittoPresence` |
| `live.ditto.transports.DittoSyncPermissions` | `com.ditto.kotlin.transports.DittoSyncPermissions` |

## Ditto Initialization (v4 vs v5)

### v4
```kotlin
val androidDependencies = DefaultAndroidDittoDependencies(context)
val identity = DittoIdentity.OnlinePlayground(
    androidDependencies,
    appId = "...",
    token = "...",
    enableDittoCloudSync = true
)
val ditto = Ditto(androidDependencies, identity)
ditto.startSync()
```

### v5
```kotlin
val config = DittoConfig(
    databaseId = "...",                          // replaces appId concept
    connect = DittoConfig.Connect.Server(url),   // or Connect.SmallPeersOnly()
)
val ditto = DittoFactory.create(config)
ditto.sync.start()                               // was ditto.startSync()
```

**Key changes**:
- `DittoFactory.create(config)` replaces `Ditto(dependencies, identity)`
- `DittoConfig.Connect.Server(url)` replaces `DittoIdentity.OnlinePlayground(...)`
- `DittoConfig.Connect.SmallPeersOnly()` for offline/mesh-only
- No `DefaultAndroidDittoDependencies` needed — platform dependencies are automatic
- `ditto.sync.start()` replaces `ditto.startSync()`

## Presence API (v5)

### DittoPeer properties
- `peerKey: String` (was `peerKeyString`)
- `deviceName: String`
- `connections: List<DittoConnection>` (was direct connection types)
- `dittoSdkVersion: String?`
- `isCompatible: Boolean?`
- `isConnectedToDittoServer: Boolean`
- `os: DittoPeerOs?`
- `peerMetadata: DittoJsonSerializable.ObjectValue`
- `identityServiceMetadata: DittoJsonSerializable.ObjectValue`
- `address: DittoAddress`

### DittoConnection (new in v5)
- `id: String`
- `peer1: String` (peer key)
- `peer2: String` (peer key)
- `connectionType: DittoConnectionType`

### DittoConnectionType enum values
- `Bluetooth`
- `AccessPoint`
- `P2PWiFi`
- `WebSocket`

### DittoPresenceGraph
- `localPeer: DittoPeer`
- `remotePeers: List<DittoPeer>`
- `serializeToJson(): String` (was `json`)

### Observing Presence
```kotlin
// v4: callback-based
ditto.presence.observe { graph -> ... }

// v5: Flow-based
ditto.presence.observe()  // returns Flow<DittoPresenceGraph>
```

### Presence properties
- `ditto.presence.graph: DittoPresenceGraph` — latest snapshot
- `ditto.presence.peerMetadata` — readable/writable metadata

## Store API (v5)

### execute (was same in v4.11+)
```kotlin
// Suspending function
suspend fun execute(
    query: String,
    arguments: DittoCborSerializable.Dictionary? = null,
): DittoQueryResult
```

### registerObserver (replaces observeLocal)
```kotlin
fun registerObserver(
    query: String,
    arguments: DittoCborSerializable.Dictionary? = null,
    eventHandler: suspend (queryResult: DittoQueryResult) -> Unit,
): DittoStoreObserver
```

### DittoQueryResult
- `items: List<DittoQueryResultItem>`
- `mutatedDocumentIds(): Set<DittoCborSerializable>`

### DittoQueryResultItem
- `value: DittoCborSerializable.Dictionary` — materialized dictionary
- `jsonString(): String` — JSON representation
- `cborData(): ByteArray`

### Sync Subscriptions
```kotlin
// v4: ditto.store.collection(name).find(query).subscribe()
// v5:
val sub = ditto.sync.registerSubscription("SELECT * FROM collection_name WHERE ...")
sub.close()  // to cancel
```

## Collection Listing (__collections)

**No `__collections` system table found in v5 SDK codebase.** The data browser's collection listing approach needs to be determined. Options:
1. DQL `SELECT DISTINCT collection_name FROM __system_tables` (if exists)
2. Track collection names manually
3. Check if there's a runtime API not visible in source

**This is a key open question for Phase 2/3.**

## Dependency Version Compatibility Notes

The v5 SDK uses:
- Kotlin 2.1.0 (DittoAndroidTools uses 1.9.25)
- AGP 8.7.3 (DittoAndroidTools uses 8.7.1)
- Compose compiler: 1.5.14
- minSdk: 24 (DittoAndroidTools uses 23)
- compileSdk: 36 (DittoAndroidTools uses 33)

**Potential issues**:
- Kotlin version mismatch (1.9.25 vs 2.1.0) may cause binary compatibility issues
- minSdk bump from 23 to 24 required
- compileSdk bump from 33 to 36 likely needed

## v4 Imports Used in This Project

All `live.ditto.*` imports that need migration:
```
live.ditto.android.DefaultAndroidDittoDependencies  -> REMOVED (automatic)
live.ditto.DiskUsageItem                            -> com.ditto.kotlin.DittoDiskUsageItem
live.ditto.Ditto                                    -> com.ditto.kotlin.Ditto
live.ditto.DittoCollection                          -> REMOVED (use DQL)
live.ditto.DittoConnectionType                      -> com.ditto.kotlin.DittoConnectionType
live.ditto.DittoDocument                            -> REMOVED (use DittoQueryResultItem)
live.ditto.DittoError                               -> com.ditto.kotlin.DittoException
live.ditto.DittoIdentity                            -> REMOVED (use DittoConfig)
live.ditto.DittoLiveQuery                           -> REMOVED (use DittoStoreObserver)
live.ditto.DittoLiveQueryEvent                      -> REMOVED
live.ditto.DittoLogger                              -> com.ditto.kotlin.DittoLogger
live.ditto.DittoLogLevel                            -> com.ditto.kotlin.DittoLogLevel
live.ditto.DittoPeer                                -> com.ditto.kotlin.DittoPeer
live.ditto.DittoPendingCursorOperation              -> REMOVED (use DQL)
live.ditto.DittoPresenceGraph                       -> com.ditto.kotlin.DittoPresenceGraph
live.ditto.DittoSubscription                        -> com.ditto.kotlin.DittoSyncSubscription
live.ditto.Presence                                 -> com.ditto.kotlin.DittoPresence
live.ditto.transports.DittoSyncPermissions          -> com.ditto.kotlin.transports.DittoSyncPermissions
```
