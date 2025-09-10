package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.PaymentMethod
import com.dashlane.hermes.generated.definitions.PaymentStatus
import com.dashlane.hermes.generated.definitions.Plan
import com.dashlane.hermes.generated.definitions.PriceCurrencyCode
import javax.annotation.processing.Generated

@Generated
data class PurchaseSubscription(
    val paymentStatus: PaymentStatus,
    val paymentAmount: Int,
    val paymentMethod: PaymentMethod,
    val purchasedPlan: Plan,
    val purchasedSeatsCount: Int,
    val paymentCurrency: PriceCurrencyCode,
) : TrackingLog {
    override val name
        get() = "purchase_subscription"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["payment_status"] = paymentStatus
        collector["payment_amount"] = paymentAmount
        collector["payment_method"] = paymentMethod
        collector["purchased_plan"] = purchasedPlan
        collector["purchased_seats_count"] = purchasedSeatsCount
        collector["payment_currency"] = paymentCurrency
    }
}