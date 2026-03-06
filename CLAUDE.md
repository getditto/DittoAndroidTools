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
| `presenceviewer/` | WebView-based mesh graph visualization |

### Shared Ditto Instance Pattern

Tools receive a `Ditto` instance from the host app. Two `DittoHandler` singletons exist (`databrowser/DittoHandler.kt`, `diskusage/DittoHandler.kt`) that hold a `companion object { lateinit var ditto: Ditto }` — these are set by the composables before use.

### Version Pinning (SDK 33 Compatibility)

The root `build.gradle` forces specific dependency versions via `resolutionStrategy` to maintain SDK 33 compatibility:
- `androidx.activity` → 1.7.2
- `androidx.compose.material3` → 1.0.1
- `com.google.android.material` → 1.9.0

## Publishing

Uses JReleaser + Maven Publishing plugin. CI workflow at `.github/workflows/gradle-publish-release.yml` triggers on GitHub releases. Requires GPG signing and Maven Central credentials via environment/secrets.

The `gradle/deploy.gradle` includes custom POM processing to resolve Compose BOM versions into explicit version strings (required by Maven Central).

## Key Dependencies

Managed in `gradle/libs.versions.toml`:
- Ditto SDK: `live.ditto:ditto` (version in `[versions].ditto`)
- Kotlin: 1.9.25 with Compose compiler 1.5.15
- Compose BOM: 2023.06.01
- Kotlin Serialization (not Moshi)
