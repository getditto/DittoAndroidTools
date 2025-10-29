package live.ditto.tools.exportlogs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat

object DittoTools {

    /**
     * Triggers a request for the local device to export its logs to the Ditto Portal.
     *
     * This function updates the small peer info document in the local store, which is observed
     * by the Ditto log collector service.
     *
     * This is a suspend function and should be called from a coroutine. It performs a
     * database write operation.
     *
     * @param ditto The active Ditto instance.
     * @throws DittoError if the database write operation fails.
     */
    suspend fun uploadLogsToPortal(ditto: Ditto) {
        withContext(Dispatchers.IO) {
            val peerKey = ditto.presence.graph.localPeer.peerKeyString

            val formatter = DateTimeFormatterBuilder()
                .append(ISODateTimeFormat.dateHourMinuteSecond())
                .appendTimeZoneOffset(null, true, 2, 2)
                .toFormatter()

            val currentTime = formatter.print(DateTime.now(DateTimeZone.UTC))

            val query = """
                UPDATE __small_peer_info
                SET log_requests.device_logs.requested_at = :currentTime
                WHERE _id = :peerKey
            """.trimIndent()

            ditto.store.execute(
                query = query,
                arguments = mapOf(
                    "currentTime" to currentTime,
                    "peerKey" to peerKey
                )
            )
        }
    }

}