package com.dashlane.premium.offer.list

import android.content.res.Resources
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.OfferType.ADVANCED
import com.dashlane.premium.offer.common.model.OfferType.FAMILY
import com.dashlane.premium.offer.common.model.OfferType.FREE
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
import java.util.Currency
import java.util.Locale

internal class OffersBuilder(
    private val monthlyOfferTypes: List<OfferType>,
    private val yearlyOfferTypes: List<OfferType>,
    private val formattedOffers: List<FormattedStoreOffer>? = null,
    private val currencyFormatter: NumberFormat? = null,
    private val yearlySavings: Map<OfferType, Float?>? = null,
    private val currentOffer: CurrentOffer?,
    private val vpnMentionAllowed: Boolean,
) {

    fun build(resources: Resources): Offers {
        val monthlyOffers = getMonthlyOffers(resources)
        val yearlyOffers = getYearlyOffers(resources)
        val bestYearlySaving = yearlySavings?.bestValue()?.let { currencyFormatter?.format(it) }
        return Offers(monthlyOffers, yearlyOffers, bestYearlySaving)
    }

    private fun Map<OfferType, Float?>.bestValue() = values.filterNotNull().maxOrNull()

    private fun getMonthlyOffers(resources: Resources) =
        monthlyOfferTypes.map { type ->
            buildOffer(type = type, periodicity = MONTHLY, resources)
        }

    private fun getYearlyOffers(resources: Resources) =
        yearlyOfferTypes.map { type ->
            buildOffer(type = type, periodicity = YEARLY, resources)
        }

    private fun buildOffer(type: OfferType, periodicity: ProductPeriodicity, resources: Resources): OfferOverview {
        val productDetails: ProductDetailsWrapper? = when (periodicity) {
            MONTHLY -> formattedOffers?.find { it.offerType == type }?.monthly?.productDetails
            YEARLY -> formattedOffers?.find { it.offerType == type }?.yearly?.productDetails
        }
        return productDetails.toOfferOverview(type, periodicity, resources)
    }

    private fun OfferType.getTitle(resources: Resources) = when (this) {
        ADVANCED -> resources.getString(R.string.plans_advanced_title)
        PREMIUM -> resources.getString(R.string.plans_premium_title)
        FAMILY -> resources.getString(R.string.plans_family_title)
        FREE -> resources.getString(R.string.plans_free_title)
    }

    private fun OfferType.getDescription(resources: Resources) = when (this) {
        ADVANCED -> resources.getString(R.string.plans_advanced_description)
        PREMIUM -> resources.getString(
            R.string.plans_premium_description.takeIf { vpnMentionAllowed }
                ?: R.string.plans_premium_description_with_no_vpn_mention
        )
        FAMILY -> resources.getString(R.string.plans_family_description)
        FREE -> resources.getString(R.string.plans_free_description)
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

    private fun getCurrentPlanLabel(type: OfferType, periodicity: ProductPeriodicity, resources: Resources): String? =
        currentOffer?.let {
            if (currentOffer.type == type && currentOffer.periodicity == periodicity) {
                resources.getString(currentOffer.labelResId)
            } else {
                null
            }
        }

    private fun ProductDetailsWrapper?.toOfferOverview(
        type: OfferType,
        periodicity: ProductPeriodicity,
        resources: Resources
    ): OfferOverview = when (this) {
        is ProductDetailsWrapper.IntroductoryOfferProduct -> {
            val pricing = getPricingInfo(
                basePricingPhase = this.basePricingPhase,
                introPricingPhase = this.introductoryPricingPhase
            )
            OfferOverview.IntroductoryOffer(
                type = type,
                title = type.getTitle(resources),
                description = type.getDescription(resources),
                billedPrice = pricing?.getPriceText(resources),
                currentPlanLabel = getCurrentPlanLabel(type, periodicity, resources),
                currencyCode = this.basePricingPhase.priceCurrencyCode,
                barredPrice = pricing?.getBarredText(resources),
                additionalInfo = pricing?.getAdditionalInfoText(resources),
                discountCallOut = DiscountCallOut.getOfferCallOut(
                    basePricingPhase = this.basePricingPhase,
                    introPricingPhase = this.introductoryPricingPhase
                ).formattedText(resources)
            )
        }
        is ProductDetailsWrapper.BasePlanProduct -> {
            OfferOverview.BaseOffer(
                type = type,
                title = type.getTitle(resources),
                description = type.getDescription(resources),
                billedPrice = getPricingInfo(
                    basePricingPhase = this.basePricingPhase,
                    introPricingPhase = null,
                )?.getPriceText(resources),
                currentPlanLabel = getCurrentPlanLabel(type, periodicity, resources),
                currencyCode = this.basePricingPhase.priceCurrencyCode
            )
        }
        null -> OfferOverview.BaseOffer(
            type = type,
            title = type.getTitle(resources),
            description = type.getDescription(resources),
            billedPrice = getPricingInfo(null, null)?.getPriceText(resources),
            currentPlanLabel = getCurrentPlanLabel(type, periodicity, resources),
            currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
        )
    }
}