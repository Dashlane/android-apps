package com.dashlane.authentication.sso

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.authentication.R
import com.dashlane.authentication.sso.utils.toIdpUrl
import com.dashlane.authentication.sso.webview.NitroSsoWebView
import com.dashlane.network.NitroUrlOverride
import com.dashlane.url.toUrlDomain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GetUserSsoInfoActivity : AppCompatActivity() {
    private var login: String? = null
    private var serviceProviderUrl: String? = null
    private var isNitroProvider: Boolean = false
    private var serviceProviderStarted: Boolean = false

    private val nitroViewModel by viewModels<NitroSsoInfoViewModel>()

    @Inject
    lateinit var nitroUrlOverride: NitroUrlOverride

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = savedInstanceState ?: intent.extras

        state?.run {
            login = getString(KEY_LOGIN)
            serviceProviderUrl = getString(KEY_SERVICE_PROVIDER_URL)
            isNitroProvider = getBoolean(KEY_IS_NITRO_SSO, false)
            serviceProviderStarted = getBoolean(KEY_SERVICE_PROVIDER_STARTED, false)
        }

        if (login == null || serviceProviderUrl == null) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (serviceProviderUrl == null || login == null) {
            finishWithResult(GetSsoInfoResult.Error.CannotOpenServiceProvider)
            return
        }

        if (isNitroProvider) {
            handleNitroSsoFlow(serviceProviderUrl!!, login!!)
        } else {
            handleSelfHostedSsoFlow(serviceProviderUrl!!, login!!)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString(KEY_LOGIN, login)
            putString(KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            putBoolean(KEY_IS_NITRO_SSO, isNitroProvider)
            putBoolean(KEY_SERVICE_PROVIDER_STARTED, serviceProviderStarted)
        }
    }

    private fun handleNitroSsoFlow(nitroUrl: String, login: String) {
        val domainName = login.split("@").last()
        lifecycleScope.launch {
            nitroViewModel.nitroState.collectLatest { state ->
                when (state) {
                    is NitroSsoInfoViewModel.NitroState.Default -> {
                        nitroViewModel.authenticate(nitroUrl = nitroUrl, login = login)
                    }
                    is NitroSsoInfoViewModel.NitroState.Init -> {
                        setContentView(
                            NitroSsoWebView(
                                context = this@GetUserSsoInfoActivity,
                                trustedDomain = state.data.idpAuthorizeUrl.toUrlDomain(),
                                redirectionUrl = state.data.spCallbackUrl,
                                onSamlResponse = { samlResponse ->
                                    nitroViewModel.confirmLogin(
                                        teamUuid = state.data.teamUuid,
                                        samlResponse = samlResponse,
                                        email = login,
                                        domainName = domainName
                                    )
                                },
                                onError = { error -> finishWithResult(error) },
                                nitroUrlOverride = nitroUrlOverride
                            ).apply {
                                id = R.id.nitro_sso_webview
                                loadUrl(state.data.idpAuthorizeUrl.toIdpUrl(login).toString())
                            }
                        )
                        nitroViewModel.onWebviewReady()
                    }
                    is NitroSsoInfoViewModel.NitroState.Ready -> {
                        
                        if (findViewById<NitroSsoWebView>(R.id.nitro_sso_webview) == null) {
                            nitroViewModel.authenticate(nitroUrl = nitroUrl, login = login)
                        }
                    }
                    is NitroSsoInfoViewModel.NitroState.Loading -> {
                        
                    }
                    is NitroSsoInfoViewModel.NitroState.LoginResult -> {
                        finishWithResult(state.result)
                    }
                }
            }
        }
    }

    private fun handleSelfHostedSsoFlow(serviceProviderUrl: String, login: String) {
        if (!serviceProviderStarted) {
            val serviceProviderIntent: Intent? = SelfHostedSsoHelper.getServiceProviderIntent(serviceProviderUrl, login)

            if (serviceProviderIntent?.resolveActivity(packageManager) == null) {
                finishWithResult(GetSsoInfoResult.Error.CannotOpenServiceProvider)
                return
            }
            serviceProviderStarted = true
            startActivity(serviceProviderIntent)
        } else {
            finishWithResult(SelfHostedSsoHelper.parseResult(login, intent))
        }
    }

    private fun finishWithResult(result: GetSsoInfoResult) {
        setResult(RESULT_OK, Intent().putExtra(KEY_RESULT, result))
        finish()
    }

    companion object {
        private const val KEY_SERVICE_PROVIDER_STARTED = "service_provider_started"

        const val KEY_LOGIN = "login"
        const val KEY_SERVICE_PROVIDER_URL = "service_provider_url"
        const val KEY_IS_NITRO_SSO = "is_nitro_sso"
        const val KEY_RESULT = "result"

        fun createStartIntent(
            context: Context,
            login: String,
            serviceProviderUrl: String,
            isNitroProvider: Boolean
        ): Intent = Intent(context, GetUserSsoInfoActivity::class.java)
            .putExtra(KEY_LOGIN, login)
            .putExtra(KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            .putExtra(KEY_IS_NITRO_SSO, isNitroProvider)

        @JvmStatic
        fun createUserSsoInfoHandlingIntent(context: Context, responseUri: Uri): Intent =
            Intent(context, GetUserSsoInfoActivity::class.java)
                .setData(responseUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}