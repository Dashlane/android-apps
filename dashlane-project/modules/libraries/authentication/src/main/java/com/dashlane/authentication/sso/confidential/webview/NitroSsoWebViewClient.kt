package com.dashlane.authentication.sso.confidential.webview

import android.app.Activity
import android.graphics.Bitmap
import android.net.http.SslError
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.webkit.ClientCertRequest
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.keychain.KeyChainException
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

class NitroSsoWebViewClient(
    private val activity: Activity,
    private val redirectionUrl: String,
    private val nitroUrl: String,
    private val trustedDomain: UrlDomain,
    private val validatedDomains: List<UrlDomain>,
    private val onError: (GetSsoInfoResult.Error) -> Unit,
    private val post: (Runnable) -> Unit,
    private val jsInterfaceName: String,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
) : WebViewClient() {

    private val navigationAllowList: Set<UrlDomain>
        get() = mutableSetOf(trustedDomain).apply {
            addAll(validatedDomains)
        }.map { it.root }.toSet()

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (url == "$nitroUrl$NITRO_CALLBACK_PATH") {
            
            return
        } else if (!url.isUrlAllowed()) {
            onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
            return
        }
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return if (request?.url.toString() == redirectionUrl) {
            post {
                view?.evaluateJavascript(
                    "javascript:${readSamlResponse(redirectionUrl = redirectionUrl, jsInterfaceName = jsInterfaceName)}",
                    null
                )
            }
            
            WebResourceResponse("text/html", "utf-8", "".byteInputStream())
        } else {
            super.shouldInterceptRequest(view, request)
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        handler?.cancel()
        onError(GetSsoInfoResult.Error.UnauthorizedNavigation)
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        val callback = KeyChainAliasCallback { alias ->
            coroutineScope.launch(ioDispatcher) {
                if (alias == null) {
                    request.ignore()
                    return@launch
                }
                handleCertRequest(alias, request)
            }
        }
        KeyChain.choosePrivateKeyAlias(activity, callback, null, null, request.host, request.port, null)
    }

    @VisibleForTesting
    fun handleCertRequest(alias: String, request: ClientCertRequest) {
        try {
            val privateKey = KeyChain.getPrivateKey(activity.baseContext, alias)
            val certificateChain = KeyChain.getCertificateChain(activity.baseContext, alias)
                ?: throw KeyChainException("Certificate content is empty")
            request.proceed(privateKey, certificateChain)
        } catch (e: InterruptedException) {
            handleCertificateError(e, request)
        } catch (e: KeyChainException) {
            handleCertificateError(e, request)
        } catch (e: IllegalStateException) {
            handleCertificateError(e, request)
        }
    }

    private fun handleCertificateError(e: Throwable, request: ClientCertRequest) {
        request.ignore()
    }

    @Language("JS")
    private fun readSamlResponse(redirectionUrl: String, jsInterfaceName: String): String = """
            $jsInterfaceName.init();
            
            const form = document.querySelector('form[action="$redirectionUrl"]');
            
            
            if (form) {
                const saml = (form.querySelector('[name="SAMLResponse"]')).value;
                $jsInterfaceName.onSamlResponseIntercepted(saml);
                form.remove();
            } else {
                $jsInterfaceName.onMissingSamlResponse();
            }
        """.trimIndent()

    private fun String?.isUrlAllowed(): Boolean =
        this?.toUrlDomainOrNull()?.root in navigationAllowList

    companion object {
        private const val NITRO_CALLBACK_PATH = "/saml/callback"
        private const val DASHLANE_LOGGER_TAG = "CONFIDENTIAL_SSO"
    }
}