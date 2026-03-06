package live.ditto.tools.presencedegradationreporter.model

import com.ditto.kotlin.serialization.DittoCborSerializable
import live.ditto.tools.presencedegradationreporter.usecase.GetDateFromTimestampUseCase

data class Settings(
    val expectedPeers: Int = 0,
    val reportApiEnabled: Boolean = false,
    val hasSeenExpectedPeers: Boolean = false,
    val sessionStartedAt: Long = 0L,
) {
    val sessionStartedAtFormatted = GetDateFromTimestampUseCase().invoke(sessionStartedAt)
}

fun Settings.toMap() = mapOf(
    "_id" to "settings",
    "expectedPeers" to expectedPeers.toString(),
    "reportApiEnabled" to reportApiEnabled.toString(),
    "hasSeenExpectedPeers" to hasSeenExpectedPeers.toString(),
    "sessionStartedAt" to sessionStartedAt.toString(),
)

fun DittoCborSerializable.Dictionary.toSettings() = Settings(
    expectedPeers = this["expectedPeers"].stringOrNull?.toIntOrNull() ?: 0,
    reportApiEnabled = this["reportApiEnabled"].stringOrNull?.toBoolean() ?: false,
    hasSeenExpectedPeers = this["hasSeenExpectedPeers"].stringOrNull?.toBoolean() ?: false,
    sessionStartedAt = this["sessionStartedAt"].stringOrNull?.toLongOrNull() ?: 0L,
)
