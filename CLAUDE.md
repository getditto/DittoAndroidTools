# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DittoAndroidTools is an Android library providing diagnostic/debug UI tools for the Ditto SDK. Built with Jetpack Compose (Material 3). Published to Maven Central as `live.ditto:ditto-tools-android`.

Two modules:
- `DittoToolsAndroid/` — Library (namespace: `live.ditto.tools`)
- `app/` — Demo application (namespace: `live.ditto.dittotoolsapp`)

## Build Commands

```bash
./gradlew assembleDebug                                      # Full debug build
./gradlew :DittoToolsAndroid:assembleDebug                   # Library only
./gradlew :DittoToolsAndroid:assembleRelease                 # Library release
./gradlew :app:assembleDebug                                 # Demo app
./gradlew publishToMavenLocal                                # Publish to local Maven
./gradlew publishReleasePublicationToLocalStagingRepository  # Staging for release
./gradlew test                                               # Unit tests
./gradlew connectedAndroidTest                               # Instrumented tests
```

## Local Setup

Create `local.properties` in the repo root with Ditto credentials for the demo app:
```properties
ditto.onlinePlayground.appId="YOUR_APPID"
ditto.onlinePlayground.token="YOUR_TOKEN"
ditto.onlinePlayground.customAuthUrl="YOUR_AUTHURL"
ditto.onlinePlayground.websocketUrl="YOUR_WEBSOCKETURL"
```

## Architecture

### Entry Point

`DittoToolsViewer` composable (`toolsviewer/DittoToolsViewer.kt`) is the main entry point. It accepts a `Ditto` instance and provides navigation to all tools via Jetpack Navigation Compose.

### Tool Components

All tools live under `DittoToolsAndroid/src/main/java/live/ditto/tools/`:

| Component | Purpose |
|---|---|
| `databrowser/` | Browse collections and documents in Ditto store |
| `diskusage/` | Display Ditto disk space metrics |
| `exportlogs/` | Export logs and upload to Ditto Portal |
| `exporter/` | Zip and share data export utilities |
| `health/` | Health checks: permissions, Bluetooth, WiFi status |
| `heartbeat/` | Periodic heartbeat with presence data |
| `logviewer/` | View and tail log files |
| `peerslist/` | List connected mesh peers |
| `presencedegradationreporter/` | Detect and report mesh degradation |
| `presenceviewer/` | WebView-based mesh graph visualization (assets copied from `getditto/ditto/sdks/kotlin/ditto-presenceviewer`) |

### Shared Ditto Instance Pattern

Tools receive a `Ditto` instance from the host app. Two `DittoHandler` singletons exist (`databrowser/DittoHandler.kt`, `diskusage/DittoHandler.kt`) that hold a `companion object { lateinit var ditto: Ditto }` — these are set by the composables before use.

## Publishing

Uses JReleaser + Maven Publishing plugin. CI workflow at `.github/workflows/gradle-publish-release.yml` triggers on GitHub releases. Requires GPG signing and Maven Central credentials via environment/secrets.

The `gradle/deploy.gradle` includes custom POM processing to resolve Compose BOM versions into explicit version strings (required by Maven Central).

## Key Dependencies

Managed in `gradle/libs.versions.toml`:
- Ditto SDK: `com.ditto:ditto-kotlin` (version in `[versions].ditto`)
- Kotlin: 2.1.0 with bundled Compose compiler plugin
- Compose BOM: 2023.06.01
- Kotlin Serialization (not Moshi)

## Demo App on Device

- Package ID: `live.ditto.DittoToolsApp` (note capitalization)
- Activity: `live.ditto.dittotoolsapp.MainActivity`
- Launch: `adb shell am start -n live.ditto.DittoToolsApp/live.ditto.dittotoolsapp.MainActivity`
- Test devices: Pixel 3 (`89UX0GNQ1`), Galaxy S20 FE (`RFCN90XKR4X`)

## v4 to v5 Migration Reference

The Ditto Kotlin SDK v5 introduces breaking changes. See `PHASE_1.md` for the full mapping.

Key changes:
- **Artifact**: `live.ditto:ditto` → `com.ditto:ditto-kotlin`
- **Package**: `live.ditto.*` → `com.ditto.kotlin.*` (transports: `com.ditto.kotlin.transports.*`)
- **Initialization**: `Ditto(dependencies, identity)` → `DittoFactory.create(DittoConfig(...))`
- **Presence**: callback-based `observe {}` → Flow-based `observe()` returning `Flow<DittoPresenceGraph>`
- **Store**: `observeLocal {}` → `registerObserver(query) {}` with DQL; `DittoDocument` → `DittoQueryResultItem`
- **Subscriptions**: `DittoSubscription` → `DittoSyncSubscription` via `ditto.sync.registerSubscription()`
- **Removed types**: `DittoCollection`, `DittoDocument`, `DittoLiveQuery`, `DittoIdentity`, `DefaultAndroidDittoDependencies`
- **Renames**: `peerKeyString` → `peerKey`, `DittoError` → `DittoException`

Local v5 SDK source reference: `~/getditto/ditto/sdks/kotlin/`
