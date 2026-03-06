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

### Phase 1: Research & Setup 🔄
- [x] Determine latest Ditto Kotlin SDK v5 version — `com.ditto:ditto-kotlin:5.0.0-preview.5`
- [x] Verify exact v5 API for presence, store, config (full results in `PHASE_1.md`)
- [ ] Update dependency in `gradle/libs.versions.toml` (group: `com.ditto`, artifact: `ditto-kotlin`, version: `5.0.0-preview.5`)
- [ ] Update Kotlin version to 2.1.0, minSdk to 24, compileSdk to 36, update Compose compiler plugin accordingly
- [ ] Attempt `./gradlew assembleDebug` and collect all compilation errors

### Phase 2: Migrate Core Data Layer ⏳
- [ ] Delete/replace `presencedegradationreporter/ditto/DittoExt.kt` (remove v4 `observeLocalAsFlow`)
- [ ] Migrate `presencedegradationreporter/model/Settings.kt` (remove `DittoDocument` dependency)
- [ ] Migrate `presencedegradationreporter/model/Peer.kt` (remove `DittoDocument` dependency)
- [ ] Migrate `presencedegradationreporter/model/PeerConnectedUpdate.kt` (remove `DittoDocument`)
- [ ] Migrate `presencedegradationreporter/repositories/PeersRepository.kt` to v5 DQL
- [ ] Migrate `heartbeat/Heartbeat.kt` to v5 store.execute() + confirm presence API

### Phase 3: Migrate DataBrowser ⏳
- [ ] Migrate `databrowser/CollectionsViewModel.kt` to v5 `registerObserver("SELECT * FROM __collections")`
- [ ] Migrate `databrowser/DocumentsViewModel.kt` to v5 `registerObserver` + DQL queries
- [ ] Migrate `databrowser/Collections.kt` (remove `DittoCollection` usage)

### Phase 4: Build & Verify ⏳
- [ ] Run `./gradlew assembleDebug` — confirm clean build
- [ ] Run `./gradlew :DittoToolsAndroid:assembleRelease` — confirm library builds
- [ ] Smoke test on device (Pixel 3 or Galaxy S20 FE)
- [ ] Update `README.md` if API surface for consumers changed

### Phase 5: Pre-merge Cleanup ⏳
- [ ] Delete `PLAN.md`, `PHASE_1.md`, and any other temporary planning files
- [ ] Commit cleanup and push before merging to `main`

## What Just Happened (2026-03-06)

Phase 1 research completed. Key findings:
- v5 SDK coordinates: `com.ditto:ditto-kotlin:5.0.0-preview.5` (on Maven Central)
- Package renamed from `live.ditto` to `com.ditto.kotlin`
- Major API changes: `DittoFactory.create()`, `DittoConfig`, Flow-based presence, no `DittoDocument`/`DittoCollection`
- No `__collections` system table found — data browser collection listing needs alternative approach
- Kotlin version bump (1.9.25 -> 2.1.0), minSdk bump (23 -> 24), compileSdk bump (33 -> 36) needed
- Full research saved to `PHASE_1.md`
- v4 app built and installed successfully on Pixel 3 and Galaxy S20 FE — confirmed working baseline before migration

## Key Learnings

- `app/DittoToolsApplication.kt` already calls `store.execute("ALTER SYSTEM SET DQL_STRICT_MODE = false")` and `disableSyncWithV3()` — the app layer is already v5-aware
- `exportlogs/DittoTools.kt` already uses DQL execute — good reference for v5 pattern
- `DittoExt.kt` in presencedegradationreporter is the main bridge that enables `observeLocalAsFlow` — replacing this is key to unblocking the whole presencedegradationreporter migration
- v5 presence API returns `Flow<DittoPresenceGraph>` instead of callback — aligns better with Compose
- `DittoPeer.peerKeyString` renamed to `DittoPeer.peerKey`
- `DittoConnection` is a new type wrapping connection details (replaces direct connection type lists)
- `store.registerObserver()` callback is `suspend` — uses coroutines natively

## Decisions Made

- **Collection listing**: Use `SELECT * FROM __collections` — try it at runtime.
- **Kotlin version**: Bump to 2.1.0 to match v5 SDK build.
