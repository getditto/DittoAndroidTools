package live.ditto.tools.diskusage.usecase

import live.ditto.tools.diskusage.DITTO_REPLICATION
import live.ditto.tools.diskusage.DITTO_STORE
import live.ditto.tools.diskusage.DiskUsageState
import live.ditto.tools.diskusage.FIVE_HUNDRED_MEGABYTES_IN_BYTES
import live.ditto.tools.diskusage.METRIC_NAME
import live.ditto.tools.diskusage.ROOT_PATH
import live.ditto.tools.diskusage.TOTAL_SIZE
import live.ditto.tools.healthmetrics.HealthMetric

class GetDiskUsageMetrics {
    val metricName: String = METRIC_NAME
    var unhealthySizeInBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES

    fun execute(currentState: DiskUsageState): HealthMetric {

        val dittoStoreSize: Int =
            currentState.children.firstOrNull { shortRelativePath(it.relativePath) == DITTO_STORE }?.sizeInBytes ?: 0
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
        val totalBytes = dittoStoreSize + dittoReplicationSize

        return totalBytes <= unhealthySizeInBytes
    }

    private fun shortRelativePath(path: String): String {
        return path.substringAfterLast('/')
    }
}
