package live.ditto.tools.diskusage

import android.annotation.SuppressLint
import kotlin.math.log10
import kotlin.math.pow

data class StorageBreakdown(
    var totalOnDiskBytes: Int = 0,
    var storeBytes: Int = 0,
    var attachmentBytes: Int = 0,
    var logsBytes: Int = 0,
    var replicationBytes: Int = 0
) {
    companion object {
        @SuppressLint("DefaultLocale")
        fun formatBytes(bytes: Int): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
            val value = bytes / 1024.0.pow(digitGroups.toDouble())
            return String.format("%.1f %s", value, units[digitGroups])
        }
    }
}