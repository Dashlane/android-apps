package com.dashlane.authentication.sso.webview

import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView



class LimitedWebChromeClient : WebChromeClient() {
    
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        result?.cancel()
        return true
    }

    
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return hideAlert(result)
    }

    
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        return hideAlert(result)
    }

    private fun hideAlert(result: JsResult?): Boolean {
        result?.cancel()
        return true
    }
}
