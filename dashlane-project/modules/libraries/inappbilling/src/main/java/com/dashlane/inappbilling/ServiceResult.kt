package com.dashlane.inappbilling

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

sealed class ServiceResult {
    sealed class Success : ServiceResult() {
        data class Products(val productDetailsList: List<ProductDetails>) : Success()

        data class Consume(val purchaseToken: String) : Success()

        data class Purchases(val purchases: List<Purchase>) : Success()
    }

    sealed class Failure(val name: String?) : ServiceResult() {
        object FeatureNotSupported : Failure("FeatureNotSupported")

        object ServiceDisconnected : Failure("ServiceDisconnected")

        object UserCanceled : Failure("UserCanceled")

        object ServiceUnavailable : Failure("ServiceUnavailable")

        object BillingUnavailable : Failure("BillingUnavailable")

        object ItemUnavailable : Failure("ItemUnavailable")

        object DeveloperError : Failure("DeveloperError")

        object Error : Failure("Error")

        object ItemAlreadyOwn : Failure("ItemAlreadyOwn")

        object ItemNotOwn : Failure("ItemNotOwn")

        object ServiceTimeout : Failure("ServiceTimeout")
    }
}

internal fun Int.isSuccess() = this == BillingClient.BillingResponseCode.OK

internal fun Int.toFailureServiceResult(): ServiceResult.Failure = when (this) {
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> ServiceResult.Failure.BillingUnavailable
    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> ServiceResult.Failure.DeveloperError
    BillingClient.BillingResponseCode.ERROR -> ServiceResult.Failure.Error
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> ServiceResult.Failure.FeatureNotSupported
    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ServiceResult.Failure.ItemAlreadyOwn
    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ServiceResult.Failure.ItemNotOwn
    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ServiceResult.Failure.ItemUnavailable
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ServiceResult.Failure.ServiceDisconnected
    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> ServiceResult.Failure.ServiceTimeout
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ServiceResult.Failure.ServiceUnavailable
    BillingClient.BillingResponseCode.USER_CANCELED -> ServiceResult.Failure.UserCanceled
    else -> ServiceResult.Failure.Error
}

internal fun Int.canRetry(): Boolean = when (this) {
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
    BillingClient.BillingResponseCode.ERROR -> true
    else -> false
}