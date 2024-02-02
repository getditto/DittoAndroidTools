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
  implementation 'live.ditto:dittopresenceviewer:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittopresenceviewer</artifactId>
    <version>0.0.2</version>
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
  implementation 'live.ditto:dittodatabrowser:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto.</groupId>
    <artifactId>dittodatabrowser</artifactId>
    <version>0.0.2</version>
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
DittoExportLogs(ditto = ditto)
```

 <img src="/Img/exportLogs.png" alt="Export Logs Image" width="300">  

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittoexportlogs:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittoexportlogs</artifactId>
    <version>0.0.2</version>
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
  implementation 'live.ditto:dittodiskusage:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>dittodiskusage</artifactId>
    <version>0.0.2</version>
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
  implementation 'live.ditto:health:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto</groupId>
    <artifactId>health</artifactId>
    <version>0.0.2</version>
</dependency>
```

### 6. Heartbeat

The Ditto Heartbeat tool allows you to monitor, locally or remotely, the peers in your mesh.

**Configure Heartbeat**

There are three values you need to provide to the Heartbeat:
1. Id/Id's - Provide all the Id's needed in order to identify a peer
2. Interval - The frequency at which the Heartbeat will scrape the data
3. Collection Name - The Ditto collection you want to add this data to

There is a `HeartbeatConfig` data class you can use to construct your configuration.

```kotlin
// Provided with the Heartbeat tool
data class HeartbeatConfig(
    val id: Map<String, String>,
    val interval: Long,
    val collectionName: String,
)

// Example:
// User defines the values here
// Passed into Heartbeat tool
val config = HeartbeatConfig(
    id = mapOf(
        "storeId" to "Tulsa, OK",
        "deviceId" to "123abc"
    ),
    interval = 30000, //ms
    collectionName = "devices",
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
    _id: {
        *passed in by user + dittoPeerKey
    },
    interval: String,
    remotePeersCount: Int,
    lastUpdated: String (ISO-8601)
    presence: {
        <peerKey>: {
            deviceName: String,
            isConnectedToDittoCloud: Bool,
            bluetooth: Int,
            p2pWifi: Int,
            lan: Int,
        },
        <peerKey>…,
        …
    }
}
```

**Callback:**

You will receive a `HeartbeatInfo` data class back
```kotlin
data class HeartbeatInfo(
    val id: Map<String, String>,
    val lastUpdated: String,
    val presence: Presence?,
)

data class Presence(
    val remotePeersCount: Int,
    val peers: List<DittoPeer>,
)
```

**Download**

Gradle:
```kotlin
dependencies {
  implementation 'live.ditto:dittoheartbeat:0.0.2'
}
```

Maven:
```
<dependency>
    <groupId>live.ditto.</groupId>
    <artifactId>dittoheartbeat</artifactId>
    <version>0.0.2</version>
</dependency>
```

## License

MIT
