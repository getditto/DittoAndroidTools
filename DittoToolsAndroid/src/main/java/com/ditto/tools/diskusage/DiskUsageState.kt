package com.ditto.tools.diskusage

data class DiskUsageState(
    val rootPath: String = "ditto",
    val totalSizeInBytes: Long = 0L,
    val totalSize: String = "Calculating...",
    val children: List<DiskUsage> = listOf(DiskUsage()),
    val unhealthySizeInBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES
)