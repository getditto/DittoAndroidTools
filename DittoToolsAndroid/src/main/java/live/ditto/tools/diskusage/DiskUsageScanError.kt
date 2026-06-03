package live.ditto.tools.diskusage

/**
 * Errors specific to collection scanning and sampling DQL queries.
 */
enum class DiskUsageScanError(val message: String) {
    EMPTY_RESULT("The query returned no result."),
    UNEXPECTED_RESULT_FORMAT("The query returned an unexpected result shape.")
}