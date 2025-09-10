package com.dashlane.inappbilling

import android.os.Parcelable
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateReference(
    val productId: String,
    val oldPurchaseToken: String,
    @ReplacementMode val mode: Int,
) : Parcelable