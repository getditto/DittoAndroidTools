package live.ditto.tools.diskusage

import android.annotation.SuppressLint
import java.text.NumberFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * Shared formatting helpers. Each method wraps a formatter so we don't
 * churn through instances at render time.
 */
object Format {
    /** Byte-size strings like "4.2 MB". */
    @SuppressLint("DefaultLocale")
    fun bytes(bytes: Int): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f %s", value, units[digitGroups])
    }

    /** Decimal count strings like "12,847". */
    fun count(value: Int): String {
        return NumberFormat.getNumberInstance().format(value)
    }
}