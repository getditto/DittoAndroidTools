package live.ditto.health.usecase

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import live.ditto.transports.DittoSyncPermissions
import kotlin.coroutines.coroutineContext

class GetDittoMissingPermissionsFlow(
    private val context: Context,
) {
    operator fun invoke(timeoutMs: Long = 300) = flow {
        while (coroutineContext.isActive) {
            emit(DittoSyncPermissions(context).missingPermissions())
            delay(timeoutMs)
        }
    }
}