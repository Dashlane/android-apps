package com.dashlane.premium.offer.list.model

import android.content.Context
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.utils.PriceUtils.MICRO
import com.dashlane.premium.utils.toFormattedPrice
import com.dashlane.ui.model.TextResource
import java.util.Locale

sealed class Pricing {
    abstract val basePlanData: Data
    abstract fun getBarredText(context: Context): String?
    abstract fun getPriceText(context: Context): String
    abstract fun getAdditionalInfoText(context: Context): String?

    internal fun getLocale(context: Context) = Locale(context.getString(R.string.language_iso_639_1))
    internal fun basePlanFormattedPrice(context: Context): String =
        (basePlanData.priceValueMicro / MICRO.toDouble()).toFormattedPrice(
            basePlanData.currencyCode,
            getLocale(context)
        )

    data class Base(
        override val basePlanData: Data
    ) : Pricing() {
        override fun getBarredText(context: Context): String? = null

        override fun getPriceText(context: Context): String {
            val periodicitySuffixString = context.getString(basePlanData.periodicity.suffixRes)
            return basePlanFormattedPrice(context) + periodicitySuffixString
        }

        override fun getAdditionalInfoText(context: Context): String? = null
    }

    sealed class Discount : Pricing() {
        abstract val introData: Data

        internal fun introOfferUnitFormattedPrice(context: Context): String {
            return (introData.priceValueMicro.toDouble() / introData.cycleLength / MICRO).toFormattedPrice(
                introData.currencyCode,
                getLocale(context)
            )
        }

        internal fun introOfferFormattedPrice(context: Context): String {
            return (introData.priceValueMicro / MICRO.toDouble()).toFormattedPrice(
                introData.currencyCode,
                getLocale(context)
            )
        }

        data class MatchingPricingCycle(
            override val basePlanData: Data,
            override val introData: Data
        ) : Discount() {
            override fun getBarredText(context: Context): String = basePlanFormattedPrice(context)

            override fun getPriceText(context: Context): String {
                val periodicitySuffixString = context.getString(introData.periodicity.suffixRes)
                return introOfferUnitFormattedPrice(context) + periodicitySuffixString
            }

            override fun getAdditionalInfoText(context: Context): String? = null
        }

        data class MismatchPricingCycle(
            override val basePlanData: Data,
            override val introData: Data
        ) : Discount() {
            override fun getBarredText(context: Context): String? = null

            override fun getPriceText(context: Context): String {
                val offerTotalLength = introData.cycleCount * introData.cycleLength
                val formattedPrice = introOfferUnitFormattedPrice(context)
                val isRecurringPayment = introData.cycleCount > 1
                val introPeriodicitySuffixString = context.getString(introData.periodicity.suffixRes)
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
                    ).format(context.resources)
                } else {
                    val textResource = when (introData.periodicity) {
                        ProductPeriodicity.MONTHLY -> R.plurals.plans_offers_mismatch_cycles_single_payment_price_monthly
                        ProductPeriodicity.YEARLY -> R.plurals.plans_offers_mismatch_cycles_single_payment_price_yearly
                    }
                    TextResource.PluralsText(
                        pluralsRes = textResource,
                        quantity = offerTotalLength,
                        args = listOf(
                            TextResource.Arg.StringArg(introOfferFormattedPrice(context) + introPeriodicitySuffixString),
                            TextResource.Arg.IntArg(offerTotalLength)
                        )
                    ).format(context.resources)
                }
            }

            override fun getAdditionalInfoText(context: Context): String {
                val periodicitySuffixString = context.getString(basePlanData.periodicity.suffixRes)
                return context.getString(
                    R.string.plans_offers_mismatch_cycles_info,
                    basePlanFormattedPrice(context),
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
