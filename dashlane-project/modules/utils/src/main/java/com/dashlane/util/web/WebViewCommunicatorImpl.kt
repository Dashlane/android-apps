package com.dashlane.util.web

import android.os.Handler
import android.os.Looper
import android.webkit.WebView



class WebViewCommunicatorImpl : WebViewCommunicator {

    val handler = Handler(Looper.getMainLooper())

    override fun injectJavascript(webView: WebView, js: String, callbackBlock: () -> Unit) {
        handler.post {
            webView.evaluateJavascript(js) { callbackBlock.invoke() }
        }
    }

    override fun execute(block: () -> Unit) {
        handler.post {
            block.invoke()
        }
    }
}