package com.dashlane.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dashlane.R
import com.dashlane.exception.NotLoggedInException
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.plans.SubscriptionCodeService
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
import com.dashlane.util.launchUrl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.Retrofit

class SettingPrivacySetting(
    private val context: Context,
    @LegacyWebservicesApi private val retrofit: Retrofit,
    private val sessionManager: SessionManager,
    private val toaster: Toaster
) {

    private val subscriptionCodeService =
        retrofit.create(SubscriptionCodeService::class.java)
    private var deferredSubscriptionCodeResponse: Deferred<SubscriptionCodeService.SubscriptionCodeResponse>
    private var showPrivacyJob: Job? = null

    init {
        
        deferredSubscriptionCodeResponse = fetchSubscriptionCodeAsync()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun open() {
        if (showPrivacyJob != null) return
        showPrivacyJob = GlobalScope.launch(Dispatchers.Main) {
            val subscriptionCode = try {
                withTimeout(1_000L) {
                    
                    deferredSubscriptionCodeResponse.await()
                        .content?.get("subscriptionCode")?.asString
                }
            } catch (t: Throwable) {
                null
            }
            if (subscriptionCode != null) {
                val uri = Uri.parse("https://www.dashlane.com/privacy/settings/?subCode=$subscriptionCode")
                context.launchUrl(uri)
            } else {
                toaster.show(R.string.network_error, Toast.LENGTH_SHORT)

                
                deferredSubscriptionCodeResponse = fetchSubscriptionCodeAsync()
            }
            showPrivacyJob = null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchSubscriptionCodeAsync(): Deferred<SubscriptionCodeService.SubscriptionCodeResponse> {
        val session = sessionManager.session
        return session?.uki?.let { uki ->
            GlobalScope.async {
                subscriptionCodeService.getSubscriptionCode(session.userId, uki)
            }
        } ?: CompletableDeferred<SubscriptionCodeService.SubscriptionCodeResponse>(null).apply {
            completeExceptionally(NotLoggedInException())
        }
    }
}