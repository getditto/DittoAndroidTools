package live.ditto.exporter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Use Case to compress a directory.
 */
class ZipFolderUseCase {
    /**
     * @param inputDirectory: Directory to be compressed
     * @param outputZipFile: File with the compressed directory
     * @param dispatcher: Coroutine Dispatcher to execute the job
     */
    suspend operator fun invoke(
        inputDirectory: File,
        outputZipFile: File,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) = withContext(dispatcher) {
        val outputStream = BufferedOutputStream(FileOutputStream(outputZipFile))

        ZipOutputStream(outputStream).use { zos ->
            inputDirectory.walkTopDown().forEach { file ->
                val zipFileName = file.absolutePath
                    .removePrefix(inputDirectory.absolutePath)
                    .removePrefix(File.separator)
                val entrySuffix = if (file.isDirectory) File.separator else ""
                val entry = ZipEntry("$zipFileName$entrySuffix")

                zos.putNextEntry(entry)

                if (file.isFile) {
                    file.inputStream().copyTo(zos)
                }
            }
        }
    }
}
