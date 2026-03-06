# Project: Update DittoAndroidTools to Ditto Kotlin SDK v5

**Branch:** `kj/sdks-1489/ditto-android-tools-v5`
**Status:** In Progress
**Linear Issue:** https://linear.app/ditto/issue/SDKS-1489/update-dittoandroidtools-to-use-ditto-kotlin-sdk-v5

## Objective

Migrate the DittoAndroidTools library from Ditto SDK v4 (`4.11.6`) to v5. The library must compile
and function correctly with the v5 Kotlin/Android SDK.

**Success criteria:**
- `gradle/libs.versions.toml` has a v5 ditto version
- The library builds without errors
- The demo app builds and runs
- All v4 store/presence API calls replaced with v5 equivalents

## Current State

SDK version in `gradle/libs.versions.toml`: `ditto = "4.11.6"`

**Already migrated to v5 API** (no changes needed):
- `app/DittoToolsApplication.kt` — uses `store.execute()`, `disableSyncWithV3()`
- `exportlogs/DittoTools.kt` — uses DQL `store.execute()`
- `peerslist/PeerListViewModel.kt` — uses `presence.observe {}`, `DittoPeer` (verify still valid in v5)
- `presenceviewer/PresenceViewModel.kt` — uses `presence.observe {}` (verify still valid)

**Still using v4 API** (must be migrated):
- `databrowser/CollectionsViewModel.kt` — `store.collections().observeLocal {}`, `DittoCollection`, `DittoSubscription`
- `databrowser/DocumentsViewModel.kt` — `store.collection().findAll().observeLocal {}`, `store.collection().find().observeLocal {}`
- `databrowser/Collections.kt` — uses `DittoCollection`
- `heartbeat/Heartbeat.kt` — `store.collection().upsert()`, `presence.graph.localPeer.peerKeyString`, `DittoPeer`, `DittoConnectionType`
- `toolsviewer/HeartbeatScreen.kt` — uses Ditto v4 types
- `presencedegradationreporter/ditto/DittoExt.kt` — defines `observeLocalAsFlow` using `DittoPendingCursorOperation`, `DittoLiveQuery`, `DittoDocument`
- `presencedegradationreporter/repositories/PeersRepository.kt` — `store[name]`, `store.write {}`, `observeLocalAsFlow`, `DittoDocument`
- `presencedegradationreporter/model/Settings.kt` — uses `DittoDocument`
- `presencedegradationreporter/model/Peer.kt` — uses `DittoDocument`
- `presencedegradationreporter/model/PeerConnectedUpdate.kt` — uses `DittoDocument`

## v4 → v5 API Mapping (VERIFIED)

| v4 | v5 |
|---|---|
| `Ditto(dependencies, identity)` | `DittoFactory.create(DittoConfig(...))` |
| `ditto.startSync()` | `ditto.sync.start()` |
| `DittoIdentity.OnlinePlayground(...)` | `DittoConfig.Connect.Server(url)` |
| `DefaultAndroidDittoDependencies(ctx)` | Removed — automatic |
| `ditto.store.collection(name).upsert(doc)` | `ditto.store.execute("INSERT INTO $name DOCUMENTS (:doc)", args)` |
| `ditto.store.collections().observeLocal {}` | `ditto.store.registerObserver("SELECT * FROM __collections") { result -> }` |
| `ditto.store[name].findAll().observeLocal {}` | `ditto.store.registerObserver("SELECT * FROM $name") { result -> }` |
| `ditto.store[name].findAll().remove()` | `ditto.store.execute("EVICT FROM $name WHERE true")` |
| `ditto.store.write { tx -> ... }` | Multiple `ditto.store.execute()` calls |
| `DittoDocument` | `DittoQueryResultItem` (.value, .jsonString()) |
| `DittoCollection` | Removed — use DQL strings |
| `DittoSubscription` | `DittoSyncSubscription` via `ditto.sync.registerSubscription(query)` |
| `DittoLiveQuery` / `observeLocal` | `DittoStoreObserver` via `store.registerObserver()` |
| `presence.observe { graph -> }` | `presence.observe()` returns `Flow<DittoPresenceGraph>` |
| `presence.graph.localPeer.peerKeyString` | `presence.graph.localPeer.peerKey` |
| `DittoConnectionType` values | `Bluetooth`, `AccessPoint`, `P2PWiFi`, `WebSocket` |
| `DittoError` | `DittoException` (sealed class) |
| `DiskUsageItem` | `DittoDiskUsageItem` |

**Package**: `live.ditto.*` → `com.ditto.kotlin.*` (transports: `com.ditto.kotlin.transports.*`)

**Reference**: The local directory `~/getditto/ditto` contains a snapshot of the Ditto v5 codebase and can be used as a reference when generating code for the v5 SDK API.

## Tasks

### Phase 1: Research & Setup ✅
- [x] Determine latest Ditto Kotlin SDK v5 version — `com.ditto:ditto-kotlin:5.0.0-preview.5`
- [x] Verify exact v5 API for presence, store, config (full results in `PHASE_1.md`)
- [x] Update dependency in `gradle/libs.versions.toml` (group: `com.ditto`, artifact: `ditto-kotlin`, version: `5.0.0-preview.5`)
- [x] Update Kotlin version to 2.1.0, minSdk to 24, compileSdk to 36, update Compose compiler plugin accordingly
- [x] Attempt `./gradlew assembleDebug` and collect all compilation errors

### Phase 2: Migrate Core Data Layer ✅
- [x] Delete/replace `presencedegradationreporter/ditto/DittoExt.kt` (remove v4 `observeLocalAsFlow`)
- [x] Migrate `presencedegradationreporter/model/Settings.kt` (remove `DittoDocument` dependency)
- [x] Migrate `presencedegradationreporter/model/Peer.kt` (remove `DittoDocument` dependency)
- [x] Migrate `presencedegradationreporter/model/PeerConnectedUpdate.kt` (remove `DittoDocument`)
- [x] Migrate `presencedegradationreporter/repositories/PeersRepository.kt` to v5 DQL
- [x] Migrate `heartbeat/Heartbeat.kt` to v5 store.execute() + confirm presence API

### Phase 3: Migrate DataBrowser ✅
- [x] Migrate `databrowser/CollectionsViewModel.kt` to v5 `registerObserver("SELECT * FROM __collections")`
- [x] Migrate `databrowser/DocumentsViewModel.kt` to v5 `registerObserver` + DQL queries
- [x] Migrate `databrowser/Collections.kt` (remove `DittoCollection` usage)

### Phase 4: Build & Verify 🔄
- [x] Run `./gradlew assembleDebug` — library and app both build clean
- [x] Run `./gradlew :DittoToolsAndroid:assembleRelease` — library builds clean
- [x] Presence Viewer — v5 JS assets replaced, renders local peer on Galaxy S20 FE
- [ ] Add license notices for bundled presence viewer JS dependencies (Hammer.js MIT, vis-network Apache 2.0/MIT, core-js MIT) — old `main.js.LICENSE.txt` was removed when v5 assets replaced v4; this is a public repo so licenses must be included
- [ ] Smoke test remaining tools on device (Galaxy S20 FE)
- [ ] Update `README.md` if API surface for consumers changed

### Phase 5: Data Browser Investigation 🔄
- [ ] Investigate why `system:collections` returns a different set of collections than v4's `__collections` (v4 shows `__presence`, `__feature_flags`; v5 shows `__presence`, `dittotools_devices`, `pdr_local_peer`, `pdr_remote_peers`) — may need SDK team input on what `system:collections` is expected to return
- [x] Fix document display: values were only extracted as string/long/boolean/double primitives, falling through to `null` for complex CBOR types (maps, arrays, byte strings). Added `cborToDisplayValue()` helper with `toString()` fallback.
- [x] Fix document attributes not showing on initial load: `docProperties` LiveData was read via `.value` instead of `observeAsState()`, so Compose never recomposed when properties arrived. Fixed in `Documents.kt`.
- [x] Fix text color in DocItem for dark theme: property names and values now use `MaterialTheme.colorScheme.onBackground`.

### Phase 6: Pre-merge Cleanup ⏳
- [ ] Delete `PLAN.md`, `PHASE_1.md`, and any other temporary planning files
- [ ] Commit cleanup and push before merging to `main`

## What Just Happened (2026-03-06 continued)

Data Browser fixes:
- Changed collection listing from `SELECT * FROM __collections` (v4 internal table) to `SELECT name FROM system:collections` (v5 virtual collection). Switched from `registerObserver` to polling with `store.execute()` since virtual collections don't support observers.
- Fixed document display: `DittoCborSerializable` values for complex types (maps, arrays, byte strings) were falling through all primitive extractors to `null`. Added `cborToDisplayValue()` helper that falls back to `toString()` for non-primitive CBOR types.
- Fixed `docProperties` LiveData not being observed by Compose (`Documents.kt`): was using `.value` instead of `observeAsState()`, so attributes never appeared on initial load.
- Fixed dark theme text visibility in `DocItem`: property names and values now use `MaterialTheme.colorScheme.onBackground`.
- Collection list discrepancy between v4 and v5 persists — added investigation task to Phase 5.

## What Just Happened (2026-03-06)

Full v5 migration completed in one session. Both library and demo app build cleanly.

Key changes applied:
- `gradle/libs.versions.toml`: ditto `4.11.6` → `5.0.0-preview.5`, Kotlin `1.9.25` → `2.1.0`, compileSdk 33 → 36, minSdk 23 → 24
- Compose compiler: switched from `kotlinCompilerExtensionVersion` to bundled Kotlin Compose plugin (Kotlin 2.x)
- All `live.ditto.*` imports replaced with `com.ditto.kotlin.*`
- `presencedegradationreporter/ditto/DittoExt.kt` deleted (v4-only)
- All v4 store/presence APIs replaced with v5 DQL + Flow equivalents
- `DittoCborSerializable.Dictionary` used in place of `DittoDocument` for model parsing
- `DittoException` not exported from Android AAR — catch blocks widened to `Exception`
- `ditto.appId` removed in v5 — `ExportLogsToPortal` now shows `localPeer.peerKey`
- SDK 33 forced dependency pins removed from root `build.gradle`

Device testing in progress on Galaxy S20 FE (v5), Pixel 3 remains on v4.

Fixes applied during testing:
- `DittoConfig.Connect.Server` requires `https://` URL, not `wss://` — auth was failing with "URL scheme is not allowed"
- `auth.expirationHandler` must be set before `sync.start()` — v5 throws `AuthenticationException` otherwise
- v4 presence viewer JS assets replaced with v5 (inline JS in index.html) — v4 JS couldn't parse v5 JSON format

Still needed:
- Smoke test remaining tools (data browser, disk usage, heartbeat, peers list, export logs, health, presence degradation reporter)
- README update (if consumer API changed)

## Key Learnings

- `app/DittoToolsApplication.kt` already calls `store.execute("ALTER SYSTEM SET DQL_STRICT_MODE = false")` and `disableSyncWithV3()` — the app layer is already v5-aware
- `exportlogs/DittoTools.kt` already uses DQL execute — good reference for v5 pattern
- `DittoExt.kt` in presencedegradationreporter is the main bridge that enables `observeLocalAsFlow` — replacing this is key to unblocking the whole presencedegradationreporter migration
- v5 presence API returns `Flow<DittoPresenceGraph>` instead of callback — aligns better with Compose
- `DittoPeer.peerKeyString` renamed to `DittoPeer.peerKey`
- `DittoConnection` is a new type wrapping connection details (replaces direct connection type lists)
- `store.registerObserver()` callback is `suspend` — uses coroutines natively
- **v5 auth (ALL SDKs)**: `auth.expirationHandler` MUST be set before `sync.start()` when using `DittoConfig.Connect.Server` — omitting it throws `AuthenticationException`. Saved to `android-dev` and `ditto-sdk-dev` skills.
- **Server URL**: Must be `https://`, not `wss://` — SDK derives websocket URL internally. Pattern: `"https://$appId.cloud.ditto.live"`
- **Presence viewer JS**: v5 ships new JS assets (inline in `index.html`) incompatible with v4 `main.js` — must replace assets when migrating

## Decisions Made

- **Collection listing**: Use `SELECT * FROM __collections` — try it at runtime.
- **Kotlin version**: Bump to 2.1.0 to match v5 SDK build.
