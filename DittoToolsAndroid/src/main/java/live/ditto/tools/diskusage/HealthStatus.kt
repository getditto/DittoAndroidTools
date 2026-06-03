package live.ditto.tools.diskusage

import androidx.compose.ui.graphics.Color

/**
 * Three-state health indicator derived from currentBytes versus a
 * configured threshold.
 */
enum class HealthStatus {
    HEALTHY,
    WARNING,
    UNHEALTHY;

    companion object {
        /** 80% of threshold — warn before the limit is hit. */
        const val WARNING_RATIO = 0.8

        /** At or above the threshold. */
        const val UNHEALTHY_RATIO = 1.0

        fun from(currentBytes: Int, thresholdBytes: Int): HealthStatus {
            if (thresholdBytes <= 0) return HEALTHY
            val ratio = currentBytes.toDouble() / thresholdBytes
            return when {
                ratio >= UNHEALTHY_RATIO -> UNHEALTHY
                ratio >= WARNING_RATIO -> WARNING
                else -> HEALTHY
            }
        }
    }

    val label: String
        get() = when (this) {
            HEALTHY -> "Healthy"
            WARNING -> "Approaching threshold"
            UNHEALTHY -> "Over threshold"
        }

    val tint: Color
        get() = when (this) {
            HEALTHY -> Color(0xFF4CAF50)
            WARNING -> Color(0xFFFF9800)
            UNHEALTHY -> Color.Red
        }
}