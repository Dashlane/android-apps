package com.dashlane.authentication.sso.confidential.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.network.NitroUrlOverride
import com.dashlane.url.UrlDomain
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

@SuppressLint("ViewConstructor")
class NitroSsoWebView(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val nitroUrlOverride: NitroUrlOverride?,
    private val trustedDomain: UrlDomain,
    private val validatedDomains: List<UrlDomain>,
    private val onSamlResponse: (String) -> Unit,
    activity: Activity,
    redirectionUrl: String,
    onError: (GetSsoInfoResult.Error) -> Unit
) : WebView(activity.baseContext) {

    private val nitroUrl: String
        get() = if (nitroUrlOverride?.nitroStagingEnabled == true) {
            nitroUrlOverride.nitroUrl ?: NITRO_DEFAULT_URL
        } else {
            NITRO_DEFAULT_URL
        }

    init {
        
        CookieManager.getInstance().removeAllCookies(null)

        this.apply {
            
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            
            addJavascriptInterface(
                SamlCatcherJavascriptInterface(
                    onSamlResponse = onSamlResponse,
                    onError = onError
                ),
                JS_INTERFACE
            )
            webChromeClient = LimitedWebChromeClient()
            webViewClient = NitroSsoWebViewClient(
                activity = activity,
                redirectionUrl = redirectionUrl,
                nitroUrl = nitroUrl,
                onError = onError,
                validatedDomains = validatedDomains,
                trustedDomain = trustedDomain,
                post = ::post,
                jsInterfaceName = JS_INTERFACE,
                ioDispatcher = ioDispatcher,
                coroutineScope = coroutineScope
            )
        }
    }

    internal class SamlCatcherJavascriptInterface(
        private val onSamlResponse: (String) -> Unit,
        private val onError: (GetSsoInfoResult.Error) -> Unit
    ) {

        @JavascriptInterface
        fun init() = Unit

        @JavascriptInterface
        fun onSamlResponseIntercepted(samlResponse: String) {
            onSamlResponse(samlResponse)
        }

        @JavascriptInterface
        fun onMissingSamlResponse() {
            onError(GetSsoInfoResult.Error.SamlResponseNotFound)
        }
    }

    companion object {
        private const val JS_INTERFACE = "Android"
        private const val NITRO_DEFAULT_URL = "https://sso.nitro.dashlane.com"
    }
}