package live.ditto.tools.presencedegradationreporter.model

import live.ditto.DittoDocument
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

fun DittoDocument.toSettings() = Settings(
    expectedPeers = this["expectedPeers"].stringValue.toInt(),
    reportApiEnabled = this["reportApiEnabled"].stringValue.toBoolean(),
    hasSeenExpectedPeers = this["hasSeenExpectedPeers"].stringValue.toBoolean(),
    sessionStartedAt = this["sessionStartedAt"].stringValue.toLong(),
)
