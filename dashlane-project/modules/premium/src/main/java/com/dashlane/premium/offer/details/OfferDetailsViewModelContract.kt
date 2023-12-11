package com.dashlane.premium.offer.details

import androidx.annotation.StringRes
import com.android.billingclient.api.Purchase
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inappbilling.ConnectionScope
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import com.dashlane.premium.offer.common.model.OfferDetails
import kotlinx.coroutines.flow.Flow

internal interface OfferDetailsViewModelContract {

    @get:StringRes
    val titleResId: Int

    val showProgressFlow: Flow<Boolean>

    val offerDetailsFlow: Flow<OfferDetails?>

    val currentPageViewFlow: Flow<AnyPage>

    val purchaseCheckingCoordinator: PurchaseCheckingCoordinator

    fun onInAppPurchaseStarted(product: OfferDetails.Product)

    fun onPurchaseDone(product: OfferDetails.Product, purchase: Purchase)
    fun onBillingServiceResult(serviceResult: ServiceResult, product: OfferDetails.Product): Purchase?

    suspend fun getBillingServiceConnection(): ConnectionScope?
}