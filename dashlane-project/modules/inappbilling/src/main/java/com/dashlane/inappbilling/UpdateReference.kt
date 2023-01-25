package com.dashlane.inappbilling

import android.os.Parcelable
import com.android.billingclient.api.BillingFlowParams.ProrationMode
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateReference(
    val productId: String,
    val oldPurchaseToken: String,
    @ProrationMode val mode: Int
) : Parcelable