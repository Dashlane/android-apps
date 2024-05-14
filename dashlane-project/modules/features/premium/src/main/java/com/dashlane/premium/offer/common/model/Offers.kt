package com.dashlane.premium.offer.common.model

import android.content.Context
import com.dashlane.inappbilling.UpdateReference
import com.dashlane.premium.R
import com.dashlane.premium.offer.list.model.OfferOverview
import com.dashlane.premium.utils.toFormattedPrice
import com.dashlane.ui.model.TextResource
import java.util.Locale

sealed class OffersState {
    object NoStoreOffersResultAvailable : OffersState()
    object NoValidOfferAvailable : OffersState()
}

internal data class Offers(
    val monthlyOffers: List<OfferOverview>,
    val yearlyOffers: List<OfferOverview>,
    val bestYearlySaving: String?
) : OffersState()

data class OfferDetails(
    val warning: TextResource? = null,
    val benefits: List<TextResource>,
    val monthlyProduct: Product? = null,
    val yearlyProduct: Product? = null
) : OffersState() {

    data class Product(
        val productId: String,
        val priceInfo: PriceInfo,
        val productDetails: ProductDetailsWrapper,
        val enabled: Boolean,
        val update: UpdateReference?
    )

    sealed class PriceInfo {
        abstract fun getCtaString(context: Context): String
        abstract fun getMonthlyInfoString(context: Context): String?
        abstract fun getYearlyInfoString(context: Context): String?
        abstract val currencyCode: String
        abstract val baseOfferPriceValue: Float
        abstract val baseOfferPeriodicity: ProductDetailsWrapper.BillingInfo
        abstract val subscriptionOfferToken: String?

        private fun getLocate(context: Context): String = context.getString(R.string.language_iso_639_1)

        internal fun Float.toFormattedPrice(context: Context): String {
            return this.toDouble().toFormattedPrice(currencyCode, Locale(getLocate(context)))
        }

        data class BaseOffer(
            override val currencyCode: String,
            override val baseOfferPriceValue: Float,
            override val baseOfferPeriodicity: ProductDetailsWrapper.BillingInfo,
            override val subscriptionOfferToken: String
        ) : PriceInfo() {
            override fun getCtaString(context: Context): String {
                val stringRes = when (baseOfferPeriodicity.periodicity) {
                    ProductPeriodicity.MONTHLY -> R.string.plan_details_price_billed_monthly_v2
                    ProductPeriodicity.YEARLY -> R.string.plan_details_price_billed_yearly_v2
                }
                return context.getString(
                    stringRes,
                    baseOfferPriceValue.toFormattedPrice(context)
                )
            }

            override fun getMonthlyInfoString(context: Context): String? = null
            override fun getYearlyInfoString(context: Context): String? = null
        }

        data class PendingOffer(
            override val currencyCode: String,
            override val baseOfferPriceValue: Float,
            override val baseOfferPeriodicity: ProductDetailsWrapper.BillingInfo,
            override val subscriptionOfferToken: String,
            val introOfferPriceValue: Float,
            val introOfferPeriodicity: ProductDetailsWrapper.BillingInfo,
            val introOfferCycleLength: Int
        ) : PriceInfo() {
            override fun getCtaString(context: Context): String {
                val periodicity = introOfferPeriodicity.periodicity
                val offerLength = introOfferPeriodicity.length
                val offerTotalLength = offerLength * introOfferCycleLength
                val offerUnitFormattedPrice = (introOfferPriceValue / offerLength).toFormattedPrice(context)
                val introOfferPeriodicitySuffixString = context.getString(periodicity.suffixRes)
                return if (offerTotalLength == 1) {
                    val textResource = when (periodicity) {
                        ProductPeriodicity.MONTHLY -> R.plurals.plan_details_pending_offer_recurring_payment_price_monthly_v2
                        ProductPeriodicity.YEARLY -> R.plurals.plan_details_pending_offer_recurring_payment_price_yearly_v2
                    }
                    TextResource.PluralsText(
                        pluralsRes = textResource,
                        quantity = offerTotalLength,
                        args = listOf(
                            TextResource.Arg.StringArg(offerUnitFormattedPrice),
                            TextResource.Arg.StringArg(introOfferPeriodicitySuffixString),
                            TextResource.Arg.IntArg(offerTotalLength)
                        )
                    ).format(context.resources)
                } else {
                    val textResource = when (periodicity) {
                        ProductPeriodicity.MONTHLY -> R.plurals.plan_details_pending_offer_single_payment_price_monthly_v2
                        ProductPeriodicity.YEARLY -> R.plurals.plan_details_pending_offer_single_payment_price_yearly_v2
                    }
                    TextResource.PluralsText(
                        pluralsRes = textResource,
                        quantity = offerTotalLength,
                        args = listOf(
                            TextResource.Arg.StringArg(offerUnitFormattedPrice + introOfferPeriodicitySuffixString),
                            TextResource.Arg.IntArg(offerTotalLength)
                        )
                    ).format(context.resources)
                }
            }

            override fun getMonthlyInfoString(context: Context): String? {
                if (baseOfferPeriodicity.periodicity != ProductPeriodicity.MONTHLY) {
                    return null
                }
                return buildAdditionalInfoString(context)
            }

            override fun getYearlyInfoString(context: Context): String? {
                if (baseOfferPeriodicity.periodicity != ProductPeriodicity.YEARLY) {
                    return null
                }
                return buildAdditionalInfoString(context)
            }

            private fun buildAdditionalInfoString(context: Context): String {
                val baseOfferPeriodicityString = context.getString(baseOfferPeriodicity.periodicity.suffixRes)
                val baseOfferFormattedPrice = baseOfferPriceValue.toFormattedPrice(context)
                return context.getString(
                    R.string.plans_details_additional_pricing_info,
                    baseOfferFormattedPrice,
                    baseOfferPeriodicityString
                )
            }
        }
    }
}