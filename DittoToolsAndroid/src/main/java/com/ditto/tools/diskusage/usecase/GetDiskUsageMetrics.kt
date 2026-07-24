package com.ditto.tools.diskusage.usecase

import com.ditto.tools.diskusage.DITTO_REPLICATION
import com.ditto.tools.diskusage.DITTO_STORE
import com.ditto.tools.diskusage.DiskUsageState
import com.ditto.tools.diskusage.FIVE_HUNDRED_MEGABYTES_IN_BYTES
import com.ditto.tools.diskusage.METRIC_NAME
import com.ditto.tools.diskusage.ROOT_PATH
import com.ditto.tools.diskusage.TOTAL_SIZE
import com.ditto.tools.healthmetrics.HealthMetric

class GetDiskUsageMetrics {
    val metricName: String = METRIC_NAME
    var unhealthySizeInBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES

    fun execute(currentState: DiskUsageState): HealthMetric {

        val dittoStoreSize: Long =
            currentState.children.firstOrNull { shortRelativePath(it.relativePath) == DITTO_STORE }?.sizeInBytes ?: 0L
        val dittoReplicationSize: Long = currentState.children.firstOrNull {
            shortRelativePath(it.relativePath) == DITTO_REPLICATION }?.sizeInBytes ?: 0L

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

    private fun healthCheckSize(dittoStoreSize: Long, dittoReplicationSize: Long): Boolean {
        val totalBytes = dittoStoreSize + dittoReplicationSize

        return totalBytes <= unhealthySizeInBytes
    }

    private fun shortRelativePath(path: String): String {
        return path.substringAfterLast('/')
    }
}
