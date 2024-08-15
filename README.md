# DittoAndroidTools

DittoAndroidTools are diagnostic tools for Ditto. You can view connected peers, export debug logs, browse collections/documents and see Ditto's disk usage/ export this data.

These tools are available through Maven.

Issues and pull requests welcome!

## Requirements

* Android 8.0+
* Jetpack Compose

## Installing
Ditto tools are released via Maven Central. Be sure to include it in your list of repositories. 

```properties
repositories {
    mavenCentral()
}
```

### List of tools and versions

| Tool Name                        | Gradle artifact                                            |
|----------------------------------|------------------------------------------------------------|
| 1. Tools Viewer                  | `'live.ditto:ditto:dittotoolsviewer:LIBRARY_VERSION'`      |
| 2. Presence Viewer               | `'live.ditto:dittopresenceviewer:LIBRARY_VERSION'`         |
| 3. Data Browser                  | `'live.ditto:dittodatabrowser:LIBRARY_VERSION'`            |
| 4. Export Logs                   | `'live.ditto:dittoexportlogs:LIBRARY_VERSION'`             |
| 5. Disk Usage/Export Data        | `'live.ditto:dittodiskusage:LIBRARY_VERSION'`              |
| 6. Health                        | `'live.ditto:health:LIBRARY_VERSION'`                      |
| 7. Heartbeat                     | `'live.ditto:dittoheartbeat:LIBRARY_VERSION'`              |
| 8. Presence Degradation Reporter | `'live.ditto:presencedegradationreporter:LIBRARY_VERSION'` |

You can find the list of versions and release notes in the [Releases tab](https://github.com/getditto/DittoAndroidTools/releases).

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

### 1. Tools Viewer
Tools viewer is the easiest way to integrate all the tools currently available. It provides a single entry point to interact with all other tools, and includes them as a dependency.

It is available as a Composable element that requires a Ditto instance. Optional parameters include:

- `modifier`: If you need to adjust the layout
- `onExitTools`: Lambda function that will be called when the "Exit Tools" button is tapped. Use this to do any back navigation or dismissal of the tools composable if you need to.

Example code:

```kotlin

//Within a composable:
DittoToolsViewer(
    modifier = Modifier(),   // optional
    ditto = YOUR_DITTO_INSTANCE,
    onExitTools = { }   // optional
)
```

 <img src="/Img/toolsViewer.png" alt="Tools Viewer Image" width="300">  


To integrate it in a Views-based app - see instructions here: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views 

**Download**

Gradle:
```kotlin
dependencies {
    implementation 'live.ditto:dittotoolsviewer:YOUR_LIBRARY_VERSION'
}
```

### 2. Presence Viewer
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

### 4. Export Logs
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

### 5. Disk Usage/ Export Data

Disk Usage allows you to see Ditto's file space usage.  
Export Data allows you to export the Ditto directory.

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

### 6. Health

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

### 7. Heartbeat

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

### 8. Presence Degradation Reporter
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

## Testing Changes Locally

There are two ways you can test things locally. Either in the demo app, or in an external project.

### Testing in the Demo App Locally

To test your changes to a module in the demo app, simply import the local module, making sure to comment out/delete the version catalog import. For example, if you made changes to DittoDataBrowser you would

add: `implementation(project(":DittoDataBrowser"))`
remove/comment out: `implementation libs.live.ditto.databrowser`

### Testing in an External Project

1. Run `./gradlew publishToMavenLocal`
2. In your external project add the `mavenLocal()` entry to your list of repository sources
3. When importing a tool, the version will be `SNAPSHOT`

## License

MIT
