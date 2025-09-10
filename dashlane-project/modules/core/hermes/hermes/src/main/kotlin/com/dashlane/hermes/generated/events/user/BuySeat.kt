package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ErrorCheckout
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.PriceCurrencyCode
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class BuySeat(
    val purchasedSeatCount: Int,
    val flowStep: FlowStep,
    val initialSeatCount: Int,
    val hasPromo: Boolean,
    val priceAmountCents: Int,
    val errorCheckout: ErrorCheckout? = null,
    val priceCurrencyCode: PriceCurrencyCode,
) : TrackingLog {
    override val name
        get() = "buy_seat"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["purchased_seat_count"] = purchasedSeatCount
        collector["flow_step"] = flowStep
        collector["initial_seat_count"] = initialSeatCount
        collector["has_promo"] = hasPromo
        collector["price_amount_cents"] = priceAmountCents
        collector["error_checkout"] = errorCheckout
        collector["price_currency_code"] = priceCurrencyCode
    }
}