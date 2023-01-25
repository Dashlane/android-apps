package com.dashlane.premium.offer.common

import android.content.Context

interface PurchaseCheckingCoordinator {

    fun openPlayStorePurchaseChecking(
        context: Context,
        sku: String,
        purchaseOriginalJson: String,
        signature: String,
        currencyCode: String,
        price: Float,
        userLockedOut: Boolean
    )
}