package com.dashlane.inappbilling

import android.content.Context
import androidx.annotation.MainThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume



@Singleton
class BillingManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher
) : BillingManager {

    

    private var billingClient: BillingClient? = null

    

    private var isServiceConnected: Boolean = false

    

    private val purchaseChannel: Channel<ServiceResult> = Channel(Channel.RENDEZVOUS)

    

    private var connectionDeferred: Deferred<Int>? = null

    

    override fun connect() {
        initializeBillingClientIfNecessary()
        globalCoroutineScope.launch {
            startServiceConnectionIfNecessary()
        }
    }

    

    override fun disconnect() {
        billingClient?.apply {
            if (isReady) {
                endConnection()
                billingClient = null
                connectionDeferred = null
                isServiceConnected = false
            }
        }
    }

    

    override fun areSubscriptionsSupported(): Boolean =
        billingClient?.areSubscriptionsSupported() ?: false

    override suspend fun getServiceConnection(): ConnectionScope? {
        withContext(mainCoroutineDispatcher) { initializeBillingClientIfNecessary() }
        val connectionResult = startServiceConnectionIfNecessary()
        return billingClient?.let {
            val scope = ConnectionScopeImpl(it, purchaseChannel)
            when {
                connectionResult.isSuccess() -> scope
                connectionResult.canRetry() -> {
                    connectionDeferred = null
                    if (startServiceConnectionIfNecessary().isSuccess()) scope
                    else null
                }
                else -> null
            }
        }
    }

    

    private suspend fun startServiceConnection(): Int {
        return suspendCancellableCoroutine { continuation ->
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    
                    if (continuation.isActive) {
                        continuation.resume(billingResult.responseCode)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isServiceConnected = false
                }
            })
        }
    }

    

    private suspend fun startServiceConnectionIfNecessary(): Int = withTimeoutOrNull(CONNECTION_TIMEOUT_MILLIS) {
        if (isServiceConnected && billingClient?.isReady == true) BillingResponseCode.OK

        connectionDeferred?.takeUnless { it.isCancelled && it.isCompleted }
            ?.await() ?: globalCoroutineScope.async { startServiceConnection() }
            .also { connectionDeferred = it }.await()
    } ?: BillingResponseCode.ERROR

    

    @MainThread
    private fun initializeBillingClientIfNecessary() {
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener { billingResult, purchases ->
                    if (billingResult.responseCode.isSuccess() && purchases != null) {
                        purchaseChannel.trySend(ServiceResult.Success.Purchases(purchases))
                    } else {
                        purchaseChannel.trySend(billingResult.responseCode.toFailureServiceResult())
                    }
                }.enablePendingPurchases().build()
        }
    }

    companion object {
        

        private const val CONNECTION_TIMEOUT_MILLIS = 1000L
    }
}
