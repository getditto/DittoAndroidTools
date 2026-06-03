package live.ditto.tools.diskusage

/**
 * A top-level on-disk storage category, used purely for user-facing
 * glossary copy. The Inspector does not currently group bytes per
 * category — that requires SDK support beyond the documented
 * diskUsage.observe() total.
 */
enum class StorageCategory(
    val displayName: String,
    val glossary: String
) {
    STORE(
        "Store",
        "Local document data, indexes, and metadata managed by Ditto."
    ),
    ATTACHMENTS(
        "Attachments",
        "Binary attachment files stored alongside documents."
    ),
    LOGS(
        "Logs",
        "Diagnostic log files written by the Ditto SDK."
    ),
    REPLICATION(
        "Replication",
        "Working data used by sync to track peers and exchange changes."
    )
}