package ditto.live.dittopresenceviewer

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

typealias JavaScript = String

class VisJSWebViewViewModel : ViewModel() {
    val pendingInvocations = mutableStateListOf<PendingJavascriptInvocation>()
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
}

class VisJSWebViewHelper(
    private val viewModel: VisJSWebViewViewModel
) {
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
        viewModel.isBackgrounded = newIsBackgrounded
        viewModel.processPendingInvocations()
    }

    private fun enqueueInvocation(
        javascript: JavaScript,
        coalescingIdentifier: String?,
        completionHandler: (result: Result<Int>?) -> Unit?
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
            viewModel.pendingInvocations.removeAll { it.coalescingIdentifier == coalescingIdentifier }
        }

        viewModel.pendingInvocations.add(
            PendingJavascriptInvocation(
                coalescingIdentifier,
                javascript,
                completion
            )
        )

        GlobalScope.launch(Dispatchers.Main) {
            viewModel.processPendingInvocations()
        }

    }
}

@Composable
fun VisJSWebView() {
    val viewModel = viewModel<VisJSWebViewViewModel>()
//    val helper = remember { VisJSWebViewHelper(viewModel) }
//    val webViewState = rememberWebViewState(url = "file:///android_asset/dist/index.html")
    LocalContext.current

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.allowFileAccess = true
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        viewModel.isInitialLoadComplete = true
                        viewModel.processPendingInvocations()
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        val errorCode = error?.errorCode
                        val message = error?.description
                        Log.d(TAG, "an error occurred $errorCode with message $message")
                    }
                }
            }
        },
        update = {
            it.loadUrl("file:///android_asset/dist/index.html")
        }
    )
}

data class PendingJavascriptInvocation(
    val coalescingIdentifier: String?,
    val javascript: JavaScript,
    val completionHandler: (result: Result<Int>?) -> Unit?
)