package com.ditto.tools.data

data class LogConfiguration(
    val maxAge : Int,
    val maxFilesOnDisk: Int,
    val maxSize: Int
)
