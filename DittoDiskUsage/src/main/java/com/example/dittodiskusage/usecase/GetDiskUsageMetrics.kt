package com.example.dittodiskusage.usecase

import com.example.dittodiskusage.DITTO_REPLICATION
import com.example.dittodiskusage.DITTO_STORE
import com.example.dittodiskusage.DiskUsageState
import com.example.dittodiskusage.METRIC_NAME
import com.example.dittodiskusage.ROOT_PATH
import com.example.dittodiskusage.TOTAL_SIZE
import com.example.dittodiskusage.TWO_GIGABYTES_IN_MEGABYTES
import live.ditto.dittohealthmetrics.HealthMetric

class GetDiskUsageMetrics {
    val metricName: String = METRIC_NAME
    var isHealthyMBSizeLimit: Int = TWO_GIGABYTES_IN_MEGABYTES

    fun execute(currentState: DiskUsageState): HealthMetric {

        val dittoStoreSize: Int =
            currentState.children.first { shortRelativePath(it.relativePath) == DITTO_STORE }.sizeInBytes
        val dittoReplicationSize: Int = currentState.children.firstOrNull {
            shortRelativePath(it.relativePath) == DITTO_REPLICATION }?.sizeInBytes ?: 0

        val isHealthy = healthCheckSize(dittoStoreSize, dittoReplicationSize)

        val details = mutableMapOf<String, String>().apply {
            for (child in currentState.children) {
                this[shortRelativePath(child.relativePath)] = child.size
            }
            this[ROOT_PATH] = currentState.rootPath
            this[TOTAL_SIZE] = currentState.totalSize
        }

        return HealthMetric(
            isHealthy = isHealthy,
            details = details
        )
    }

    private fun healthCheckSize(dittoStoreSize: Int, dittoReplicationSize: Int): Boolean {
        val totalMB = bytesToMegabytes(dittoStoreSize + dittoReplicationSize)

        return totalMB <= isHealthyMBSizeLimit
    }

    private fun bytesToMegabytes(bytes: Int): Int {
        return bytes / 1048576
    }

    private fun shortRelativePath(path: String): String {
        return path.substringAfterLast('/')
    }
}
