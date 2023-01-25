package com.dashlane.inappbilling

import com.android.billingclient.api.BillingClient



internal fun BillingClient.areSubscriptionsSupported(): Boolean {
    return isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode.isSuccess()
}