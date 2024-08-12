package com.dashlane.premium.offer.list.model

import android.content.res.Resources
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.ui.model.TextResource
import java.text.NumberFormat
import java.util.Locale

sealed class DiscountCallOut {
    abstract fun formattedText(resources: Resources): String

    data class FixedPercent(
        private val basePriceMicro: Long,
        private val offerPriceMicro: Long,
        private val offerCycleLength: Int,
        private val offerCycleCount: Int,
        private val periodicity: ProductPeriodicity
    ) : DiscountCallOut() {

        override fun formattedText(resources: Resources): String {
            val totalLength = offerCycleLength * offerCycleCount
            val discount: Double = 1 - offerPriceMicro.toDouble() / offerCycleLength / basePriceMicro
            val discountPercent = NumberFormat.getPercentInstance(
                Locale.Builder().setLanguage(resources.getString(R.string.language_iso_639_1)).build()
            ).apply { maximumFractionDigits = 0 }.format(discount)
            val textResource = when (periodicity) {
                ProductPeriodicity.MONTHLY -> R.plurals.plans_offer_call_out_fixed_percent_discount_monthly
                ProductPeriodicity.YEARLY -> R.plurals.plans_offer_call_out_fixed_percent_discount_yearly
            }
            return TextResource.PluralsText(
                pluralsRes = textResource,
                quantity = totalLength,
                args = listOf(TextResource.Arg.StringArg(discountPercent), TextResource.Arg.IntArg(totalLength))
            ).format(resources)
        }
    }

    data class FreeTrial(
        private val offerCycleLength: Int,
        private val offerCycleCount: Int,
        private val periodicity: ProductPeriodicity
    ) : DiscountCallOut() {
        override fun formattedText(resources: Resources): String {
            val totalLength = offerCycleLength * offerCycleCount
            val textResource = when (periodicity) {
                ProductPeriodicity.MONTHLY -> R.plurals.plans_offer_call_out_free_trial_monthly
                ProductPeriodicity.YEARLY -> R.plurals.plans_offer_call_out_free_trial_yearly
            }
            return TextResource.PluralsText(
                pluralsRes = textResource,
                quantity = totalLength,
                args = listOf(TextResource.Arg.IntArg(totalLength))
            ).format(resources)
        }
    }

    data class SimpleSaving(
        private val offerCycleLength: Int,
        private val offerCycleCount: Int,
        private val periodicity: ProductPeriodicity
    ) : DiscountCallOut() {
        override fun formattedText(resources: Resources): String {
            val totalLength = offerCycleLength * offerCycleCount
            val textResource = when (periodicity) {
                ProductPeriodicity.MONTHLY -> R.plurals.plans_offer_call_out_simple_saving_over_fixed_period_monthly
                ProductPeriodicity.YEARLY -> R.plurals.plans_offer_call_out_simple_saving_over_fixed_period_yearly
            }
            return TextResource.PluralsText(
                pluralsRes = textResource,
                quantity = totalLength,
                args = listOf(TextResource.Arg.IntArg(totalLength))
            ).format(resources)
        }
    }

    companion object {
        fun getOfferCallOut(
            basePricingPhase: ProductDetailsWrapper.PricingPhase,
            introPricingPhase: ProductDetailsWrapper.PricingPhase
        ): DiscountCallOut {
            val introBillingInfo = introPricingPhase.billingInfo
            val periodicity = introBillingInfo.periodicity
            val offerCycleLength = introBillingInfo.length
            val offerCycleCount = introPricingPhase.cycleCount
            return when {
                
                introPricingPhase.priceMicro == 0L -> {
                    FreeTrial(
                        offerCycleLength = offerCycleLength,
                        offerCycleCount = offerCycleCount,
                        periodicity = periodicity
                    )
                }
                
                introBillingInfo.periodicity == basePricingPhase.billingInfo.periodicity -> {
                    FixedPercent(
                        basePriceMicro = basePricingPhase.priceMicro,
                        offerPriceMicro = introPricingPhase.priceMicro,
                        offerCycleLength = offerCycleLength,
                        offerCycleCount = offerCycleCount,
                        periodicity = periodicity
                    )
                }
                
                else -> {
                    SimpleSaving(
                        offerCycleLength = offerCycleLength,
                        offerCycleCount = offerCycleCount,
                        periodicity = periodicity
                    )
                }
            }
        }
    }
}
