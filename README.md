# DittoAndroidTools

DittoAndroidTools are diagnostic tools for Ditto. You can view connected peers, export debug logs, browse collections/documents and see Ditto's disk usage.

These tools are available through Maven.

Issues and pull requests welcome!

## Requirements

* Android 8.0+
* Jetpack Compose

## Repository
Ditto tools are deployed in Maven Central. Be sure to include it in your list of repositories. 
```properties
repositories {
    mavenCentral()
}
```

## Usage

First, you must initialize Ditto:

```kotlin
val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
val identity = DittoIdentity.OnlinePlayground(androidDependencies, appId = "YOUR_APPID", token = "YOUR_TOKEN", enableDittoCloudSync = true)
ditto = Ditto(androidDependencies, identity)
DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG
ditto.startSync()
```

_NOTICE:_ This project loads ditto's credentials from `local.properties`
```properties
ditto.onlinePlayground.appId="YOUR_APPID"
ditto.onlinePlayground.token="YOUR_TOKEN"
```


There are five components in this package: Presence Viewer, Data Browser, Export Logs, Disk Usage, Health.

### 1. Presence Viewer
The Presence Viewer displays a mesh graph that allows you to see all connected peers within the mesh and the transport that each peer is using to make a connection.  

Within a Composable, you pass ditto to the constructor:

```kotlin
DittoPresenceViewer(ditto = ditto)
```

 <img src="/Img/presenceViewer.png" alt="Presence Viewer Image" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittopresenceviewer:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittopresenceviewer</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```


### 2. Data Browser

The Ditto Data Browser allows you to view all your collections, documents within each collection and the propeties/values of a document. With the Data Browser, you can observe any changes that are made to your collections and documents in real time.  

Within a Composable function, you pass ditto to the constructor:

```kotlin
DittoDataBrowser(ditto = ditto)
```

 <img src="/Img/collections.png" alt="Collections Image" width="300">  

 <img src="/Img/document.png" alt="Document Image" width="300">  

**Standalone App**

If you are using the Data Browser as a standalone app, there is a button, Start Subscriptions, you must press in order to start syncing data. If you are embedding the Data Browser into another application then you do not need to press Start Subscriptions as you should already have your subscriptions running.

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittodatabrowser:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto.</groupId>
    <artifactId>dittodatabrowser</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

### 3. Export Logs
Export Logs allows you to export a file of the logs from your applcation.  

**Important**

Before calling `ditto.startSync()` we need to set the `DittoLogger.setLogFileURL(<logFileURL>)`. This registers a file path where logs will be written to, whenever Ditto wants to issue a log (on top of emitting the log to the console). Use the `LogFileConfig` struct:

```
object LogFileConfig {
    private const val logsDirectoryName = "debug-logs"
    private const val logFileName = "logs.txt"

    val logsDirectory: Path by lazy {
        val directory = Paths.get(System.getProperty("java.io.tmpdir"), logsDirectoryName)
        Files.createDirectories(directory)
        directory
    }

    val logFile: Path by lazy {
        logsDirectory.resolve(logFileName)
    }
}
```

and then before calling `ditto.startSync()` set the log file url with:

```
LogFileConfig.logFile.let { logFile ->
    DittoLogger.setLogFile(logFile.toString())
}
```

Now we can call `ExportLogs()`.

```kotlin
ExportLogs(onDismiss: () -> Unit)
```

 <img src="/Img/exportLogs.png" alt="Export Logs Image" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittoexportlogs:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittoexportlogs</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

### 4. Disk Usage

Disk Usage allows you to see Ditto's file space usage.  

```kotlin
DittoDiskUsage(ditto = ditto)
```

 <img src="/Img/diskUsage.png" alt="Disk Usage Image" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittodiskusage:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittodiskusage</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

### 5. Health

Health allows you to see the status of ditto's required services.

Example: Wi-Fi, Bluetooth, Missing Permissions. 

```kotlin
HealthScreen()
```

 <img src="/Img/health.png" alt="Health" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:health:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>health</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

### 6. Heartbeat

The Ditto Heartbeat tool allows you to monitor, locally or remotely, the peers in your mesh.

**Configure Heartbeat**

These are the values you need to provide to the Heartbeat:
1. `id` - Unique value that identifies the device
2. `secondsInterval` - The frequency at which the Heartbeat will scrape the data
3. `metaData` -  Optional - any metadata you wish to add to the Heartbeat
4. `healthMetricProviders` List of HealthMetricProviders
5. `publishToDittoCollection` - Optional - set to false to prevent from publishing the heartbeat to Ditto collection. Default true.

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
healthMetricProviders.add(DiskUsageViewModel())
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
    _id: <ditto peerKey>,
    _schema: String,
    secondsInterval: String,
    presenceSnapshotDirectlyConnectedPeersCount: Int,
    lastUpdated: String (ISO-8601),
    sdk: String,
    presenceSnapshotDirectlyConnectedPeers: {
        <peerKey>: {
            deviceName: String,
            sdk: String,
            isConnectedToDittoCloud: Bool,
            bluetooth: Int,
            p2pWifi: Int,
            lan: Int,
        },
        <peerKey>…,
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

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittoheartbeat:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto.</groupId>
    <artifactId>dittoheartbeat</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

### 7. Presence Degradation Reporter
Tracks the status of your mesh, allowing to define the minimum of required peers that needs to be connected.
Exposes an API to notify when the condition of minimum required peers is not met.

```kotlin
## UI Composable
PresenceDegradationReporterScreen(ditto = ditto)
```

```kotlin
## API
ditto.presenceDegradationReporterFlow().collect { state ->
    // state.settings
    // state.localPeer
    // state.remotePeers
}
```

<img src="/Img/presencedegradationreporter.png" alt="Health" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:presencedegradationreporter:YOUR_LIBRARY_VERSION'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>presencedegradationreporter</artifactId>
    <version>YOUR_LIBRARY_VERSION</version>
</dependency>
```

## License

MIT
