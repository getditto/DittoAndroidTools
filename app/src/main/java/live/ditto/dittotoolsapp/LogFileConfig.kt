package live.ditto.dittotoolsapp

import java.nio.file.*
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