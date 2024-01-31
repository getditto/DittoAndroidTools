# DittoAndroidTools

DittoAndroidTools are diagnostic tools for Ditto. You can view connected peers, export debug logs, browse collections/documents and see Ditto's disk usage.

These tools are available through Maven.

Issues and pull requests welcome!

## Requirements

* Android 8.0+
* Jetpack Compose

## Usage

First, you must initialize Ditto:

```kotlin
val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
val identity = DittoIdentity.OnlinePlayground(androidDependencies, appId = "YOUR_APPID", token = "YOUR_TOKEN", enableDittoCloudSync = true)
ditto = Ditto(androidDependencies, identity)
DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG
ditto.startSync()
```


There are four components in this package: Presence Viewer, Data Browser, Export Logs, Disk Usage.

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


## License

MIT
