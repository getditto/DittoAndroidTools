package com.example.dittodiskusage

data class DiskUsageState(
    val rootPath: String = "ditto",
    val totalSizeInBytes: Int = 0,
    val totalSize: String = "Calculating...",
    val children: List<DiskUsage> = listOf(DiskUsage()),
    val unhealthySizeInBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES
)