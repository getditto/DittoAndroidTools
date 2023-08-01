package ditto.live.dittopresenceviewer

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoPresenceObserver


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DittoPresenceViewer(ditto: Ditto) {
    val viewModel = PresenceViewModel()
    viewModel.ditto = ditto
    VisJSWebView(viewModel)
}

class PresenceViewModel : ViewModel() {
    var ditto: Ditto? = null
        set(value) {
            if (value === ditto) {
                return
            }

            peersObserver?.close()
            value?.let { ditto ->
                peersObserver = ditto.presence.observe { graph ->
                    Log.d(TAG,"network " + graph.json())
                    updateNetwork(graph.json()) {}
                }
            }
            field = value
        }

    private var peersObserver: DittoPresenceObserver? = null
    private val pendingInvocations = mutableStateListOf<PendingJavascriptInvocation>()
    var isInitialLoadComplete by mutableStateOf(false)
    var isBackgrounded by mutableStateOf(false)
    var webView by mutableStateOf<android.webkit.WebView?>(null)

    fun processPendingInvocations() {
        if (!isInitialLoadComplete || isBackgrounded) return

        for (invocation: PendingJavascriptInvocation in pendingInvocations) {
            webView?.evaluateJavascript(invocation.javascript) { result ->
                // The result parameter contains the result of the evaluation.
                // You can handle the success or failure of the evaluation here.
                if (result != null) {
                    // Evaluation was successful, handle the result
                    Log.d(TAG, "evaluateJavascript was successful - $result")
                } else {
                    // Evaluation failed
                    Log.d(TAG, "evaluateJavascript was NOT successful")
                }
            }
        }

        pendingInvocations.clear()
    }

    fun updateNetwork(json: String, completionHandler: () -> Unit?) {
        // To avoid characters in our JSON string being interpreted as JS, we pass our JSON
        // as base64 encoded string and decode on the other side.
        val base64JSON = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)

        enqueueInvocation(
            "Presence.updateNetwork('$base64JSON');",
            "updateNetwork"
        ) { result ->
            if (result != null && result.isFailure) {
                Log.d(TAG, "failed to update network " + result.exceptionOrNull()?.message)
            }
            Log.d(TAG, "updated network")

        }

        // In release mode, we should never fail (we control all inputs and outputs an
        // all resources are offline). An assertion crash should have triggered during
        // unit tests or development testing if our JS was incorrectly packaged or if
        // there was drift between the JS code and the JS function names/signatures
        // hardcoded in Kotlin.
        //
        // We log to the console to help catch errors during active development, but
        // otherwise always report success to our caller.
        completionHandler()
    }

    fun updateAppState(newIsBackgrounded: Boolean) {
        this.isBackgrounded = newIsBackgrounded
        this.processPendingInvocations()
    }

    private fun enqueueInvocation(
        javascript: JavaScript,
        coalescingIdentifier: String?,
        completionHandler: (result: Result<Int>?) -> Int
    ) {
        val completion: (Result<Int>?) -> Unit? = { result ->
            if (result != null) {
                if (result.isFailure) {
                    completionHandler(result.exceptionOrNull()?.let { Result.failure(it) })
                } else {
                    completionHandler(result.onSuccess { Result.success(it) })
                }
            }
        }

        if (!coalescingIdentifier.isNullOrEmpty()) {
            this.pendingInvocations.removeAll { it.coalescingIdentifier == coalescingIdentifier }
        }

        this.pendingInvocations.add(
            PendingJavascriptInvocation(
                coalescingIdentifier,
                javascript,
                completion
            )
        )

        GlobalScope.launch(Dispatchers.Main) {
            processPendingInvocations()
        }

    }
}


