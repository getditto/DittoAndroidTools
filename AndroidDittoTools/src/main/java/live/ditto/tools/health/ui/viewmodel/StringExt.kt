package live.ditto.health.ui.viewmodel

import java.util.Locale

fun String.capitalize(): String {
    if (this.isEmpty()) {
        return ""
    }
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }
}