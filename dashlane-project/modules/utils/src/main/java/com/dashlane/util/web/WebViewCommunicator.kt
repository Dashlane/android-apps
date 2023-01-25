package com.dashlane.util.web

import android.webkit.WebView



interface WebViewCommunicator {

    fun injectJavascript(webView: WebView, js: String, callbackBlock: () -> Unit = {})

    fun execute(block: () -> Unit)
}