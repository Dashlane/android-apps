package com.dashlane.authentication.sso.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.network.NitroUrlOverride
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomain
import com.dashlane.url.toUrlDomainOrNull
import org.intellij.lang.annotations.Language

class NitroSsoWebView(
    context: Context,
    trustedDomain: UrlDomain,
    redirectionUrl: String,
    onSamlResponse: (String) -> Unit,
    onError: (GetSsoInfoResult.Error) -> Unit,
    private val nitroUrlOverride: NitroUrlOverride?
) : WebView(context) {

    private val nitroUrl: String
        get() = if (nitroUrlOverride?.nitroStagingEnabled == true) {
            nitroUrlOverride.nitroUrl ?: NITRO_DEFAULT_URL
        } else {
            NITRO_DEFAULT_URL
        }

    constructor(context: Context) : this(
        context = context,
        trustedDomain = "dashlane.com".toUrlDomain(),
        redirectionUrl = "",
        onSamlResponse = {},
        onError = {},
        nitroUrlOverride = null
    )

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
            webViewClient = object : WebViewClient() {

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.cancel()
                    onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    return if (request?.url.toString() == redirectionUrl) {
                        post {
                            view?.evaluateJavascript(
                                "javascript:${readSamlResponse(redirectionUrl)}",
                                null
                            )
                        }
                        
                        WebResourceResponse("text/html", "utf-8", "".byteInputStream())
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (!url.isUrlAllowed(trustedDomain)) {
                        onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
                        return
                    } else if (url == "$nitroUrl$NITRO_CALLBACK_PATH") {
                        
                        return
                    }
                }
            }
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
        private const val NITRO_CALLBACK_PATH = "/saml/callback"

        @Language("JS")
        private fun readSamlResponse(redirectionUrl: String): String = """
            $JS_INTERFACE.init();
            
            const form = document.querySelector('form[action="$redirectionUrl"]');
            
            if (form) {
                $JS_INTERFACE.onSamlResponseIntercepted(form.children.SAMLResponse.value);
                form.remove(); 
            } else {
                $JS_INTERFACE.onMissingSamlResponse();
            }
        """.trimIndent()
    }
}

private fun String?.isUrlAllowed(trustedDomain: UrlDomain): Boolean {
    val domain: UrlDomain = this?.toUrlDomainOrNull()?.root ?: return false

    return domain == trustedDomain.root || domain == "dashlane.com".toUrlDomain()
}
