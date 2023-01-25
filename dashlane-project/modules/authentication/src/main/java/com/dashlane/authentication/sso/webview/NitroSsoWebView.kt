package com.dashlane.authentication.sso.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomain
import com.dashlane.url.toUrlDomainOrNull
import org.intellij.lang.annotations.Language



class NitroSsoWebView(
    context: Context,
    trustedDomain: UrlDomain,
    redirectionUrl: String,
    onSamlIntercepted: (String) -> Unit,
    onError: (GetSsoInfoResult.Error) -> Unit
) : WebView(context) {

    constructor(context: Context) : this(
        context = context,
        trustedDomain = "dashlane.com".toUrlDomain(),
        redirectionUrl = "",
        onSamlIntercepted = {},
        onError = {}
    )

    init {
        
        CookieManager.getInstance().removeAllCookies(null)

        this.apply {
            
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true

            addJavascriptInterface(
                SamlCatcherJavascriptInterface(onSamlIntercepted),
                JS_INTERFACE
            )
            webChromeClient = LimitedWebChromeClient()
            webViewClient = object : WebViewClient() {

                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.cancel()
                    onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (!url.isUrlAllowed(trustedDomain)) {
                        onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
                        return
                    }
                    view?.evaluateJavascript(
                        "javascript:${getSamlInterceptionScript(redirectionUrl)}",
                        null
                    )
                }
            }
        }
    }

    internal class SamlCatcherJavascriptInterface(private val webviewCallback: (String) -> Unit) {

        @JavascriptInterface
        fun init() = Unit

        @JavascriptInterface
        fun onSamlResponseIntercepted(samlResponse: String) {
            webviewCallback(samlResponse)
        }
    }

    companion object {

        private const val JS_INTERFACE = "Android"

        

        @Language("JS")
        private fun getSamlInterceptionScript(redirectionUrl: String): String = """
            $JS_INTERFACE.init();
            
            
            const config = { attributes: true, childList: true, subtree: true, attributeFilter: ["form"] };
            
            
            const callback = (mutationList, observer) => {
                for (const mutation of mutationList) {
                    if (mutation.type === 'childList') {
                        const form = document.querySelector('form[action="$redirectionUrl"]');
                        
                        if (form) {
                            $JS_INTERFACE.onSamlResponseIntercepted(form.children.SAMLResponse.value);
                            form.remove(); 
                        }
                    }
                }
            };
            
            
            const observer = new MutationObserver(callback);
            
            observer.observe(window.document, config);
        """.trimIndent()
    }
}



private fun String?.isUrlAllowed(trustedDomain: UrlDomain): Boolean {
    val domain: UrlDomain = this?.toUrlDomainOrNull() ?: return false

    return trustedDomain.root == domain.root
}
