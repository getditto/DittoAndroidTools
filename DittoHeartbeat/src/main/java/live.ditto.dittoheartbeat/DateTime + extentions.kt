package live.ditto.dittoheartbeat

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat

fun DateTime.toISOString(): String {
    return toString(ISODateTimeFormat.dateTimeNoMillis()) ?: run {
        val format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        this.toString(format)
    }
}