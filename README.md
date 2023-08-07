# DittoAndroidTools

 <img align="left" src="./Img/Ditto_logo.png" alt="Ditto Logo" width="150">  
 <br />  
 <br />  
 <br />  
 
DittoAndroidTools are diagnostic tools for Ditto. You can view connected peers, export debug logs, browse collections/documents and see Ditto's disk usage.

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
 

### 3. Export Logs
Export Logs allows you to export a file of the logs from your applcation.  

```kotlin
DittoExportLogs(ditto = ditto)
```

 <img src="/Img/exportLogs.png" alt="Export Logs Image" width="300">  

### 4. Disk Usage

Disk Usage allows you to see Ditto's file space usage.  

```kotlin
DittoDiskUsage(ditto = ditto)
```


 <img src="/Img/diskUsage.png" alt="Disk Usage Image" width="300">  


## License

MIT
