package com.dashlane.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dashlane.R
import com.dashlane.exception.NotLoggedInException
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.Toaster
import com.dashlane.util.launchUrl
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
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
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class SettingPrivacySetting(
    private val context: Context,
    @LegacyWebservicesApi private val retrofit: Retrofit,
    private val sessionManager: SessionManager,
    private val toaster: Toaster,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
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

        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode35(
                    type = "settings",
                    action = "goToPrivacySettings"
                )
            )

        showPrivacyJob = GlobalScope.launch(Dispatchers.Main) {
            val subscriptionCode = try {
                withTimeout(1_000L) {
                    
                    deferredSubscriptionCodeResponse.await().subscriptionCode
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

    interface SubscriptionCodeService {
        @FormUrlEncoded
        @POST("/3/premium/getSubscriptionCode")
        suspend fun getSubscriptionCode(
            @Field("login") login: String,
            @Field("uki") uki: String
        ): SubscriptionCodeResponse

        class SubscriptionCodeResponse {
            @SerializedName("code")
            val code: Int = 0

            @SerializedName("message")
            val message: String? = null

            @SerializedName("content")
            val content: JsonObject? = null

            val subscriptionCode: String? by lazy { content?.get("subscriptionCode")?.asString }
        }
    }
}