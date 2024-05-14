package com.dashlane.inappbilling

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ConnectionScopeImpl(
    private val billingClient: BillingClient,
    private val purchaseChannel: Channel<ServiceResult>
) : ConnectionScope {
    override suspend fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String?,
        updateReference: UpdateReference?
    ): ServiceResult {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder().apply {
            setProductDetails(productDetails)
            if (offerToken != null) {
                setOfferToken(offerToken)
            }
        }.build()
        val purchaseParamsBuilder = BillingFlowParams.newBuilder().apply {
            setProductDetailsParamsList(listOf(productParams))
            if (updateReference != null) {
                setSubscriptionUpdateParams(
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldPurchaseToken(updateReference.oldPurchaseToken)
                        .setReplaceProrationMode(updateReference.mode).build()
                )
            }
        }

        val purchaseParams = purchaseParamsBuilder.build()
        billingClient.launchBillingFlow(activity, purchaseParams)
        return runCatching { purchaseChannel.receive() }.getOrNull()
            ?: ServiceResult.Failure.ServiceDisconnected
    }

    override suspend fun consume(purchaseToken: String): ServiceResult {
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (continuation.isActive) {
                    if (billingResult.responseCode.isSuccess()) {
                        continuation.resume(ServiceResult.Success.Consume(purchaseToken))
                    } else {
                        continuation.resume(billingResult.responseCode.toFailureServiceResult())
                    }
                }
            }
        }
    }

    override suspend fun acknowledge(purchaseToken: String): ServiceResult {
        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (continuation.isActive) {
                    if (billingResult.responseCode.isSuccess()) {
                        continuation.resume(ServiceResult.Success.Consume(purchaseToken))
                    } else {
                        continuation.resume(billingResult.responseCode.toFailureServiceResult())
                    }
                }
            }
        }
    }

    override suspend fun queryPurchases(ioDispatcher: CoroutineDispatcher): ServiceResult {
        return withContext(ioDispatcher) {
            val inAppPurchases = queryPurchaseForType(ProductType.INAPP)
            val subscriptionPurchases = if (billingClient.areSubscriptionsSupported()) {
                queryPurchaseForType(ProductType.SUBS)
            } else {
                null
            }

            when {
                inAppPurchases.responseCode.isSuccess() && subscriptionPurchases?.responseCode?.isSuccess() == true -> {
                    ServiceResult.Success.Purchases(inAppPurchases.purchases + subscriptionPurchases.purchases)
                }
                inAppPurchases.responseCode.isSuccess() -> ServiceResult.Success.Purchases(
                    inAppPurchases.purchases
                )
                else -> inAppPurchases.responseCode.toFailureServiceResult()
            }
        }
    }

    private suspend fun queryPurchaseForType(productType: String): PurchaseResult {
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(productType).build()
            ) { purchasesResult: BillingResult, purchasesList: List<Purchase> ->
                if (continuation.isActive) {
                    continuation.resume(PurchaseResult(purchasesResult.responseCode, purchasesList))
                }
            }
        }
    }

    override suspend fun queryProductDetails(
        productList: List<String>,
        types: List<String>
    ): ServiceResult {
        return coroutineScope {
            val result = types.map { productType ->
                async { queryProductDetailsForType(productType, productList) }
            }.awaitAll()

            val productDetailsList = result.flatMap { productResult ->
                if (productResult.responseCode.isSuccess() && productResult.productDetails.isNotEmpty()) {
                    productResult.productDetails
                } else {
                    emptyList()
                }
            }
            if (result.find { it.responseCode.isSuccess() } != null) {
                ServiceResult.Success.Products(productDetailsList)
            } else {
                result.firstOrNull()?.responseCode?.toFailureServiceResult()
                    ?: ServiceResult.Failure.Error
            }
        }
    }

    private suspend fun queryProductDetailsForType(
        @ProductType itemType: String,
        productId: List<String>
    ): ProductDetailsResult {
        return suspendCancellableCoroutine { continuation ->
            val productsDetailsParams = productId.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(itemType)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productsDetailsParams)
                .build()
            billingClient.queryProductDetailsAsync(params) { billingResult: BillingResult, productDetailsList: List<ProductDetails> ->
                if (continuation.isActive) {
                    continuation.resume(
                        ProductDetailsResult(
                            billingResult.responseCode,
                            productDetailsList
                        )
                    )
                }
            }
        }
    }

    private data class PurchaseResult(
        val responseCode: Int,
        val purchases: List<Purchase>
    )

    private data class ProductDetailsResult(
        val responseCode: Int,
        val productDetails: List<ProductDetails>
    )
}