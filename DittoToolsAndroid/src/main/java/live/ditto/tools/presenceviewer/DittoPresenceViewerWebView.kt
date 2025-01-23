package live.ditto.tools.presenceviewer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@SuppressLint("ViewConstructor")
class DittoPresenceViewerWebView(
    context: Context,
    params: ViewGroup.LayoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT),
    private val coroutineScope: CoroutineScope
): WebView(context) {

    private val webViewLoadingMutex = Mutex(locked = true)
    private var updateUiJob: Job? = null

    init {
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/dist/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()

        layoutParams = params

        @Suppress("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest,
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webViewLoadingMutex.unlock()
            }
        }
        loadUrl("file:///android_asset/dist/index.html")
    }

    fun setJson(json: String) {
        updateUiJob?.cancel()
        updateUiJob = coroutineScope.launch {
            webViewLoadingMutex.withLock {
                val base64Json = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
                this@DittoPresenceViewerWebView.evaluateJavascript(
                    "Presence.updateNetwork('$base64Json');",
                ) {
                    // here is the result of evaluating the js
                }
            }
        }
    }
}