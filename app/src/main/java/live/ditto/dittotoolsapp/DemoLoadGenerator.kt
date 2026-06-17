package live.ditto.dittotoolsapp

import com.ditto.kotlin.Ditto
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DemoLoadGenerator {

    const val DEMO_COLLECTION = "demo_data"
    const val BATCH_SIZE = 25

    suspend fun insertBatch(ditto: Ditto, batchSize: Int = BATCH_SIZE) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        repeat(batchSize) { i ->
            val id = UUID.randomUUID().toString()
            ditto.store.execute(
                "INSERT INTO `$DEMO_COLLECTION` DOCUMENTS (:doc)",
                mapOf(
                    "doc" to mapOf(
                        "_id" to id,
                        "label" to "Generated at $now #$i",
                        "createdAt" to now,
                        "batchIndex" to i.toLong(),
                    )
                )
            )
        }
    }
}
