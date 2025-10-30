# DittoAndroidTools

DittoAndroidTools are diagnostic tools for Ditto. You can view connected peers, export debug logs, browse collections/documents and see Ditto's disk usage/ export this data.

These tools are available via Maven Central.

For support, please contact Ditto Support (<support@ditto.live>).

## Requirements

* Android 8.0+
* Jetpack Compose

## Installing
Ditto tools are released via Maven Central. Be sure to include it in your list of repositories.

```groovy
repositories {
    mavenCentral()
}
```
Include the tools repository:

```groovy
dependencies {
    implementation 'live.ditto:ditto-tools-android:LIBRARY_VERSION'
}
```
You can find the list of versions and release notes in the [Releases tab](https://github.com/getditto/DittoAndroidTools/releases).

*Note: The tools used to be released as individual Maven artefacts but have now been moved into a single module, and are released as such.*

## Usage

All tools require an initialized instance of Ditto to work. For example, with the Online Playground identity:

```kotlin
val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
val identity = DittoIdentity.OnlinePlayground(androidDependencies, appId = "YOUR_APPID", token = "YOUR_TOKEN", enableDittoCloudSync = true)
ditto = Ditto(androidDependencies, identity)
DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG
ditto.startSync()
```

### 1. Tools Viewer
Tools viewer is the easiest way to integrate all the tools currently available. It provides a single entry point to interact with all other tools, and includes them as a dependency.

It is available as a Composable element that requires a Ditto instance. Optional parameters include:

- `modifier`: If you need to adjust the layout
- `onExitTools`: Lambda function that will be called when the "Exit Tools" button is tapped. Use this to do any back navigation or dismissal of the tools composable if you need to.

Example code:

```kotlin
import live.ditto.tools.DittoToolsViewer
// minimum code required to get started
DittoToolsViewer(
    ditto = YOUR_DITTO_INSTANCE
)
```

 <img src="/Img/toolsViewer.png" alt="Tools Viewer Image" width="1000">  

To integrate it in a Views-based app - see instructions here: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views

### 2. Presence Viewer
The Presence Viewer displays a mesh graph that allows you to see all connected peers within the mesh and the transport that each peer is using to make a connection.

Within a Composable, you pass ditto to the constructor:

```kotlin
DittoPresenceViewer(ditto = ditto)
```

 <img src="/Img/presenceViewer.png" alt="Presence Viewer Image" width="300">  

### 3. Data Browser

The Ditto Data Browser allows you to view all your collections, documents within each collection and the propeties/values of a document. With the Data Browser, you can observe any changes that are made to your collections and documents in real time.

Within a Composable function, you pass ditto to the constructor:

```kotlin
DittoDataBrowser(ditto = ditto)
```

 <img src="/Img/collections.png" alt="Collections Image" width="300">  

 <img src="/Img/document.png" alt="Document Image" width="300">  

**Standalone App**

If you are using the Data Browser as a standalone app, there is a button, Start Subscriptions, you must press in order to start syncing data. If you are embedding the Data Browser into another application then you do not need to press Start Subscriptions as you should already have your subscriptions running.

### 4. Export Logs
Export Logs allows you to export logs from your application into a file.

Include `ExportLogs()` in your Composable function. You can pass in a lambda function to be called when the dialog is dismissed.

```kotlin
ExportLogs(onDismiss: () -> Unit)
```
<img src="/Img/exportLogs.png" alt="Export Logs Image" width="300">

### 5. Export Logs to Portal
Export Logs to Portal allows you to export logs from your application into the Ditto Portal associated with your AppID

Include `ExportLogsToPortal()` in your Composable function. You can pass in a lambda function to be called when the dialog is dismissed and a ditto object which will contain your AppID.

```kotlin
ExportLogsToPortal(ditto: Ditto, onDismiss: () -> Unit)
```

You'll also be able to use a new public API found at DittoTools.uploadLogsToPortal(ditto: Ditto) that takes a ditto object which will allow you to upload logs from anywhere in your app.

<img src="/Img/exportLogsToPortal.png" alt="Export Logs Image" width="300">

### 6. Disk Usage/ Export Data

Disk Usage allows you to see Ditto's file space usage.  
Export Data allows you to export the Ditto directory.

```kotlin
DittoDiskUsage(ditto = ditto)
```

 <img src="/Img/diskUsage.png" alt="Disk Usage Image" width="300">

### 7. Health

Health allows you to see the status of Ditto's services.

Example: WiFi/Bluetooth state/permissions, device capabilities

The default implementation is a Composable that displays all facets of information.

```kotlin
HealthScreen()
```

This Composable also takes in an optional list of `enum`'s if you need to show/hide certain groups of information. Current valid enums are:

```
TRANSPORT_HEALTH -- shows WiFi/Bluetooth status (enabled/disabled, permissions state) 
WIFI_AWARE_STATE -- displays whether the device supports WiFi Aware
```

 <img src="/Img/health.png" alt="Health" width="400">

### 8. Heartbeat

The Ditto Heartbeat tool allows you to monitor, locally or remotely, the peers in your mesh.

**Configure Heartbeat**

These are the values you need to provide to the Heartbeat:
1. `id` - Unique value that identifies the device
2. `secondsInterval` - The frequency at which the Heartbeat will scrape the data
3. `metaData` -  Optional - any metadata you wish to add to the Heartbeat
4. `healthMetricProviders` List of HealthMetricProviders
5. `publishToDittoCollection` - Optional - set to false to prevent from publishing the heartbeat to Ditto collection. Default true.

Available `healthMetricProviders`:
1. HealthViewModel() - health metrics for Permissions Health Tool
2. DiskUsageViewModel() - health metrics for Ditto disk usage. `isHealthy` is determined by the size of the `ditto_store` and `ditto_replication` folders. The default isHealthy size is 2GB, but this can be configured.

There is a `DittoHeartbeatConfig` data class you can use to construct your configuration.

```kotlin
// Provided with the Heartbeat tool
data class DittoHeartbeatConfig(
    val id: String,
    val secondsInterval: Int,
    val metaData: Map<String, Any>? = null,
    val healthMetricProviders: List<HealthMetricProvider>?,
    val publishToDittoCollection: Boolean = true // Toggle to avoid publishing
)

// Example:
// User defines the values here
// Passed into Heartbeat tool
var healthMetricProviders: MutableList<HealthMetricProvider> = mutableListOf()
val diskUsageViewModel = DiskUsageViewModel()
diskUsageViewModel.isHealthyMBSizeLimit = 2048 //2GB worth of data
healthMetricProviders.add(diskUsageViewModel)
val config = DittoHeartbeatConfig(
    id = <unique device id>,
    secondsInterval = 30, //seconds
    metaData = mapOf(
        "deviceType" to "KDS"
    ),
    healthMetricProviders = healthMetricProviders,
    publishToDittoCollection = true
)

// Provide the config and your Ditto instance to startHearbeat()
startHeartbeat(ditto, config).collect { heartbeatInfo = it }
```

**User Interface**

You will need to provide your own UI. You can see an example [here](https://github.com/getditto/DittoAndroidTools/blob/HeartBeatTool/app/src/main/java/live/ditto/dittotoolsapp/HeartbeatView.kt).

There are two ways you can access the data:
1. The Ditto collection you provided
2. startHeartBeat() provides a callback with the data

**Ditto Collection:**

This is the model of the data and what you can use for reference
```kotlin
{
    _id: <ditto peerKeyString>,
    _schema: String,
    secondsInterval: String,
    presenceSnapshotDirectlyConnectedPeersCount: Int,
    lastUpdated: String (ISO-8601),
    sdk: String,
    presenceSnapshotDirectlyConnectedPeers: {
        <peerKeyString>: {
            deviceName: String,
            sdk: String,
            isConnectedToDittoCloud: Bool,
            bluetooth: Int,
            p2pWifi: Int,
            lan: Int,
        },
        <peerKeyString>…,
        …
    },
    metaData: {},
    healthMetrics: {},
}
```

**Callback:**

You will receive a `HeartbeatInfo` data class back
```kotlin
data class DittoHeartbeatInfo(
    val id: String,
    val schema: String,
    val lastUpdated: String,
    val metaData: Map<String, Any>?,
    val secondsInterval: Int,
    val presenceSnapshotDirectlyConnectedPeersCount: Int,
    val presenceSnapshotDirectlyConnectedPeers: Map<String, Any>,
    val sdk: String,
    var healthMetrics: MutableMap<String, HealthMetric> = mutableMapOf()

)
```

### 9. Presence Degradation Reporter
Tracks the status of your mesh, allowing to define the minimum of required peers that needs to be connected.
Exposes an API to notify when the condition of minimum required peers is not met.

####  UI Composable

```kotlin

PresenceDegradationReporterScreen(ditto = ditto)
```

#### API

```kotlin
ditto.presenceDegradationReporterFlow().collect { state: PresenceDegradationReporterApiState ->
    // state.settings
    // state.localPeer
    // state.remotePeers
}
```

<img src="/Img/presencedegradationreporter.png" alt="Health" width="300">  


## Shrinking the app size then not using all tools

If you are not using all the tools, you can use the built-in R8 shrinker to remove unused code. This will reduce the size of the app.
You will need to configure Proguard to ensure the underlying Ditto SDK is not removed.

```proguard
# proguard-rules.pro

# --- Ditto SDK rules --- 
# Selective package definition will allow shrinking of all code in live.ditto.tools and its subpackages.
-keepnames class com.fasterxml.jackson.** { *; }
-keep class live.ditto.* { *; }
-keep class live.ditto.transports.** { *; }
-keep class live.ditto.internal.** { *; }
# --- End Ditto SDK rules --- 

# --- Ditto Tools names ---
# The following can be removed to obfuscate tools code further.
-keepnames class live.ditto.tools.** { *; }
# --- End Ditto Tools names ---

```

## Testing Changes Locally

There are two ways you can test things locally. Either in the demo app, or in an external project.

### Testing in the Demo App Locally

To run the demo app locally, get valid playground Ditto instance credentials and store them in `local.properties` file. You can find both the app ID and token in the [Ditto Portal](https://portal.ditto.live)

```properties
ditto.onlinePlayground.appId="YOUR_APPID"
ditto.onlinePlayground.token="YOUR_TOKEN"
ditto.onlinePlayground.customAuthUrl="YOUR_AUTHURL"
ditto.onlinePlayground.websocketUrl="YOUR_WEBSOCKETURL"
```

To test your changes to a module in the demo app, make sure to import the local module in `app/build.gradle` dependencies section:

add: `implementation(project(":DittoToolsAndroid"))`
remove/comment out: `implementation libs.live.ditto.ditto-tools-android`

### Testing in an External Project

1. Run `./gradlew publishToMavenLocal`
2. In your external project add the `mavenLocal()` entry to your list of repository sources
3. When importing a tool, the version will be `SNAPSHOT`

## License

MIT
