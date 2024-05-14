package com.dashlane.inappbilling

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.CoroutineDispatcher

interface ConnectionScope {
    suspend fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String?,
        updateReference: UpdateReference? = null
    ): ServiceResult

    suspend fun consume(purchaseToken: String): ServiceResult

    suspend fun acknowledge(purchaseToken: String): ServiceResult

    suspend fun queryPurchases(ioDispatcher: CoroutineDispatcher): ServiceResult

    suspend fun queryProductDetails(
        productList: List<String>,
        types: List<String> = listOf(BillingClient.ProductType.SUBS)
    ): ServiceResult
}