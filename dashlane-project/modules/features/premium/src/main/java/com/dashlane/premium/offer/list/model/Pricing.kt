package com.dashlane.premium.offer.list.model

import android.content.res.Resources
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.utils.PriceUtils.MICRO
import com.dashlane.premium.utils.toFormattedPrice
import com.dashlane.ui.model.TextResource
import java.util.Locale

sealed class Pricing {
    abstract val basePlanData: Data
    abstract fun getBarredText(resources: Resources): String?
    abstract fun getPriceText(resources: Resources): String
    abstract fun getAdditionalInfoText(resources: Resources): String?

    internal fun getLocale(resources: Resources) = Locale(resources.getString(R.string.language_iso_639_1))
    internal fun basePlanFormattedPrice(resources: Resources): String =
        (basePlanData.priceValueMicro / MICRO.toDouble()).toFormattedPrice(
            basePlanData.currencyCode,
            getLocale(resources)
        )

    data class Base(
        override val basePlanData: Data
    ) : Pricing() {
        override fun getBarredText(resources: Resources): String? = null

        override fun getPriceText(resources: Resources): String {
            val periodicitySuffixString = resources.getString(basePlanData.periodicity.suffixRes)
            return basePlanFormattedPrice(resources) + periodicitySuffixString
        }

        override fun getAdditionalInfoText(resources: Resources): String? = null
    }

    sealed class Discount : Pricing() {
        abstract val introData: Data

        internal fun introOfferUnitFormattedPrice(resources: Resources): String {
            return (introData.priceValueMicro.toDouble() / introData.cycleLength / MICRO).toFormattedPrice(
                introData.currencyCode,
                getLocale(resources)
            )
        }

        internal fun introOfferFormattedPrice(resources: Resources): String {
            return (introData.priceValueMicro / MICRO.toDouble()).toFormattedPrice(
                introData.currencyCode,
                getLocale(resources)
            )
        }

        data class MatchingPricingCycle(
            override val basePlanData: Data,
            override val introData: Data
        ) : Discount() {
            override fun getBarredText(resources: Resources): String = basePlanFormattedPrice(resources)

            override fun getPriceText(resources: Resources): String {
                val periodicitySuffixString = resources.getString(introData.periodicity.suffixRes)
                return introOfferUnitFormattedPrice(resources) + periodicitySuffixString
            }

            override fun getAdditionalInfoText(resources: Resources): String? = null
        }

        data class MismatchPricingCycle(
            override val basePlanData: Data,
            override val introData: Data
        ) : Discount() {
            override fun getBarredText(resources: Resources): String? = null

            override fun getPriceText(resources: Resources): String {
                val offerTotalLength = introData.cycleCount * introData.cycleLength
                val formattedPrice = introOfferUnitFormattedPrice(resources)
                val isRecurringPayment = introData.cycleCount > 1
                val introPeriodicitySuffixString = resources.getString(introData.periodicity.suffixRes)
                return if (offerTotalLength == 1 || isRecurringPayment) {
                    val textResource = when (introData.periodicity) {
                        ProductPeriodicity.MONTHLY -> R.plurals.plans_offers_mismatch_cycles_recurring_payment_price_monthly
                        ProductPeriodicity.YEARLY -> R.plurals.plans_offers_mismatch_cycles_recurring_payment_price_yearly
                    }
                    return TextResource.PluralsText(
                        pluralsRes = textResource,
                        quantity = offerTotalLength,
                        args = listOf(
                            TextResource.Arg.StringArg(formattedPrice),
                            TextResource.Arg.StringArg(introPeriodicitySuffixString),
                            TextResource.Arg.IntArg(offerTotalLength)
                        )
                    ).format(resources)
                } else {
                    val textResource = when (introData.periodicity) {
                        ProductPeriodicity.MONTHLY -> R.plurals.plans_offers_mismatch_cycles_single_payment_price_monthly
                        ProductPeriodicity.YEARLY -> R.plurals.plans_offers_mismatch_cycles_single_payment_price_yearly
                    }
                    TextResource.PluralsText(
                        pluralsRes = textResource,
                        quantity = offerTotalLength,
                        args = listOf(
                            TextResource.Arg.StringArg(introOfferFormattedPrice(resources) + introPeriodicitySuffixString),
                            TextResource.Arg.IntArg(offerTotalLength)
                        )
                    ).format(resources)
                }
            }

            override fun getAdditionalInfoText(resources: Resources): String {
                val periodicitySuffixString = resources.getString(basePlanData.periodicity.suffixRes)
                return resources.getString(
                    R.string.plans_offers_mismatch_cycles_info,
                    basePlanFormattedPrice(resources),
                    periodicitySuffixString
                )
            }
        }
    }

    data class Data(
        val periodicity: ProductPeriodicity,
        val cycleCount: Int,
        val cycleLength: Int,
        val priceValueMicro: Long,
        val currencyCode: String
    )
}
