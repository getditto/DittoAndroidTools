package live.ditto.tools.utils

class Utils {
    companion object{

        fun formatFileSize(bytes: Long) : String =
            when {
                bytes >= 1 shl 30 -> "%.1f GB".format(bytes.toDouble() / (1 shl 30))
                bytes >= 1 shl 20 -> "%.1f MB".format(bytes.toDouble() / (1 shl 20))
                bytes >= 1 shl 10 -> "%.0f kB".format(bytes.toDouble() / (1 shl 10))
                else -> "$bytes B"
            }
        }

}