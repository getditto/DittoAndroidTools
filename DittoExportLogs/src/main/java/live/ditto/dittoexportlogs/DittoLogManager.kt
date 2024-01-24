package live.ditto.dittoexportlogs

import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.isRegularFile

object Config {
    private const val logsDirectoryName = "debug-logs"
    private const val zippedLogFileName = "logs.zip"

    val logsDirectory: Path by lazy {
        val directory = Paths.get(System.getProperty("java.io.tmpdir"), logsDirectoryName)
        Files.createDirectories(directory)
        directory
    }

    val zippedLogsFile: Path by lazy {
        Paths.get(System.getProperty("java.io.tmpdir"), zippedLogFileName)
    }
}

object DittoLogManager {

    fun createLogsZip(): Path {
        try {
            Files.deleteIfExists(Config.zippedLogsFile)
            ZipOutputStream(BufferedOutputStream(Config.zippedLogsFile.toFile().outputStream())).use { out ->
                getLogFiles().forEach { log ->
                    out.putNextEntry(ZipEntry(log.toString().substring(Config.logsDirectory.toString().length + 1).replace(File.separator, "/")))
                    Files.newBufferedReader(log).useLines { lines ->
                        lines.forEach { line ->
                            out.write(line.toByteArray())
                            out.write("\n".toByteArray())
                        }
                    }
                    out.closeEntry()
                }
            }
            return Config.zippedLogsFile
        } catch (e: IOException) {
            error("Failed to zip logs: $e")
        }
    }

    private fun getLogFiles(): Stream<Path> {
        return Files.walk(Config.logsDirectory)
            .filter { it.isRegularFile() }
    }
}
