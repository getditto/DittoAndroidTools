package live.ditto.tools.utils

import android.content.Context
import android.text.format.Formatter

class Utils {
    companion object{
        fun formatFileSize(context: Context?, bytes: Long) : String{
            return Formatter.formatShortFileSize(context, bytes)
        }
    }
}