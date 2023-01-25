package com.dashlane.plans.ui.view

import android.content.Context
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import javax.inject.Inject

class PurchaseCheckingCoordinatorImpl @Inject constructor() : PurchaseCheckingCoordinator {

    override fun openPlayStorePurchaseChecking(
        context: Context,
        sku: String,
        purchaseOriginalJson: String,
        signature: String,
        currencyCode: String,
        price: Float,
        userLockedOut: Boolean
    ) {
        context.startActivity(
            PurchaseCheckingActivity.newIntentForPlayStorePurchase(
                context = context,
                purchasePlanId = sku,
                purchaseOriginalJson = purchaseOriginalJson,
                signature = signature,
                currencyCode = currencyCode,
                totalPrice = price,
                userLockedOut = userLockedOut
            )
        )
    }
}