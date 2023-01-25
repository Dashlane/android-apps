package com.dashlane.premium.offer.list

import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.OfferType.ADVANCED
import com.dashlane.premium.offer.common.model.OfferType.FAMILY
import com.dashlane.premium.offer.common.model.OfferType.PREMIUM
import com.dashlane.premium.offer.common.model.Offers
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.common.model.ProductPeriodicity.MONTHLY
import com.dashlane.premium.offer.common.model.ProductPeriodicity.YEARLY
import com.dashlane.premium.offer.list.model.CurrentOffer
import com.dashlane.premium.offer.list.model.DiscountCallOut
import com.dashlane.premium.offer.list.model.OfferOverview
import com.dashlane.premium.offer.list.model.Pricing
import java.text.NumberFormat

internal class OffersBuilder(
    private val monthlyOfferTypes: List<OfferType>,
    private val yearlyOfferTypes: List<OfferType>,
    private val formattedOffers: List<FormattedStoreOffer>? = null,
    private val currencyFormatter: NumberFormat? = null,
    private val yearlySavings: Map<OfferType, Float?>? = null,
    private val currentOffer: CurrentOffer?,
    private val vpnMentionAllowed: Boolean,
    private val supportIntroductoryOffers: Boolean
) {

    fun build(): Offers {
        val monthlyOffers = getMonthlyOffers()
        val yearlyOffers = getYearlyOffers()
        val bestYearlySaving = yearlySavings?.bestValue()?.let { currencyFormatter?.format(it) }
        return Offers(monthlyOffers, yearlyOffers, bestYearlySaving)
    }

    private fun Map<OfferType, Float?>.bestValue() = values.filterNotNull().maxOrNull()

    private fun getMonthlyOffers() =
        monthlyOfferTypes.map { type ->
            buildOffer(type = type, periodicity = MONTHLY)
        }

    private fun getYearlyOffers() =
        yearlyOfferTypes.map { type ->
            buildOffer(type = type, periodicity = YEARLY)
        }

    private fun buildOffer(type: OfferType, periodicity: ProductPeriodicity): OfferOverview {
        val productDetails: ProductDetailsWrapper? = when (periodicity) {
            MONTHLY -> formattedOffers?.find { it.offerType == type }?.monthly?.productDetails
            YEARLY -> formattedOffers?.find { it.offerType == type }?.yearly?.productDetails
        }
        return productDetails.toOfferOverview(type, periodicity)
    }

    private fun OfferType.getTitle() = when (this) {
        ADVANCED -> R.string.plans_advanced_title
        PREMIUM -> R.string.plans_premium_title
        FAMILY -> R.string.plans_family_title
    }

    private fun OfferType.getDescription() = when (this) {
        ADVANCED -> R.string.plans_advanced_description
        PREMIUM -> R.string.plans_premium_description.takeIf { vpnMentionAllowed }
            ?: R.string.plans_premium_description_with_no_vpn_mention
        FAMILY -> R.string.plans_family_description
    }

    private fun getPricingInfo(
        basePricingPhase: ProductDetailsWrapper.PricingPhase?,
        introPricingPhase: ProductDetailsWrapper.PricingPhase?,
    ): Pricing? {
        basePricingPhase ?: return null

        val basePricing = Pricing.Data(
            priceValueMicro = basePricingPhase.priceMicro,
            cycleCount = basePricingPhase.cycleCount,
            cycleLength = basePricingPhase.billingInfo.length,
            periodicity = basePricingPhase.billingInfo.periodicity,
            currencyCode = basePricingPhase.priceCurrencyCode
        )

        if (introPricingPhase == null) {
            return Pricing.Base(
                basePlanData = basePricing
            )
        }

        val introPricing = Pricing.Data(
            priceValueMicro = introPricingPhase.priceMicro,
            cycleCount = introPricingPhase.cycleCount,
            cycleLength = introPricingPhase.billingInfo.length,
            periodicity = introPricingPhase.billingInfo.periodicity,
            currencyCode = basePricingPhase.priceCurrencyCode
        )

        return if (basePricing.periodicity == introPricing.periodicity) {
            Pricing.Discount.MatchingPricingCycle(
                basePlanData = basePricing,
                introData = introPricing
            )
        } else {
            Pricing.Discount.MismatchPricingCycle(
                basePlanData = basePricing,
                introData = introPricing
            )
        }
    }

    private fun getOnGoingRes(type: OfferType, periodicity: ProductPeriodicity) =
        currentOffer?.let {
            if (currentOffer.type == type && currentOffer.periodicity == periodicity) {
                currentOffer.labelResId
            } else {
                null
            }
        }

    private fun ProductDetailsWrapper?.toOfferOverview(
        type: OfferType,
        periodicity: ProductPeriodicity
    ): OfferOverview {
        if (!supportIntroductoryOffers) {
            return buildOfferWithoutIntroOffers(type, periodicity)
        }

        return when (this) {
            is ProductDetailsWrapper.IntroductoryOfferProduct -> {
                OfferOverview.IntroductoryOffer(
                    type = type,
                    title = type.getTitle(),
                    description = type.getDescription(),
                    pricing = getPricingInfo(
                        basePricingPhase = this.basePricingPhase,
                        introPricingPhase = this.introductoryPricingPhase
                    ),
                    onGoingRes = getOnGoingRes(type, periodicity),
                    discountCallOut = DiscountCallOut.getOfferCallOut(
                        basePricingPhase = this.basePricingPhase,
                        introPricingPhase = this.introductoryPricingPhase
                    )
                )
            }
            is ProductDetailsWrapper.BasePlanProduct -> {
                OfferOverview.BaseOffer(
                    type = type,
                    title = type.getTitle(),
                    description = type.getDescription(),
                    pricing = getPricingInfo(
                        basePricingPhase = this.basePricingPhase,
                        introPricingPhase = null
                    ),
                    onGoingRes = getOnGoingRes(type, periodicity)
                )
            }
            null -> OfferOverview.BaseOffer(
                type = type,
                title = type.getTitle(),
                description = type.getDescription(),
                pricing = getPricingInfo(null, null),
                onGoingRes = getOnGoingRes(type, periodicity)
            )
        }
    }

    private fun ProductDetailsWrapper?.buildOfferWithoutIntroOffers(
        type: OfferType,
        periodicity: ProductPeriodicity
    ): OfferOverview {
        return if (this == null) {
            OfferOverview.BaseOffer(
                type = type,
                title = type.getTitle(),
                description = type.getDescription(),
                pricing = getPricingInfo(null, null),
                onGoingRes = getOnGoingRes(type, periodicity)
            )
        } else {
            OfferOverview.BaseOffer(
                type = type,
                title = type.getTitle(),
                description = type.getDescription(),
                pricing = getPricingInfo(
                    basePricingPhase = this.basePricingPhase,
                    introPricingPhase = null
                ),
                onGoingRes = getOnGoingRes(type, periodicity)
            )
        }
    }
}