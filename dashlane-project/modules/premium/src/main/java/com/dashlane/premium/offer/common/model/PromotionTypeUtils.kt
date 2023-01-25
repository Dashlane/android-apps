package com.dashlane.premium.offer.common.model

import com.dashlane.premium.R



fun ProductDetailsWrapper.IntroductoryOfferProduct.toPromotionType(
    offerType: OfferType
): IntroOfferType? {
    return when {
        isMonthlyFreeTrial() -> IntroOfferType.MatchingCycleFreeTrial(
            offerType = offerType,
            productDetails = this
        )
        isFreeTrialForYearlyPlan() -> IntroOfferType.MismatchingCycleFreeTrial(
            offerType = offerType,
            productDetails = this
        )
        isMonthlySinglePayment() -> IntroOfferType.MonthlySinglePayment(
            offerType = offerType,
            productDetails = this
        )
        isMonthlySinglePaymentOfferRenewingToYearly() -> IntroOfferType.MonthlySinglePaymentToYearly(
            offerType = offerType,
            productDetails = this
        )
        isMonthlyRecurringPayment() -> IntroOfferType.MonthlyRecurringPayment(
            offerType = offerType,
            productDetails = this
        )
        isYearlySinglePayment() -> IntroOfferType.YearlySinglePaymentToYearly(
            offerType = offerType,
            productDetails = this
        )
        isMonthlyRecurringPaymentOfferRenewingToYearly() -> IntroOfferType.MonthlyRecurringPaymentToYearly(
            offerType = offerType,
            productDetails = this
        )
        isYearlyRecurringPayment() -> IntroOfferType.YearlyRecurringPaymentToYearly(
            offerType = offerType,
            productDetails = this
        )
        else -> null
    }
}

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isYearlyRecurringPayment() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.cycleCount > 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isMonthlyRecurringPaymentOfferRenewingToYearly() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.billingInfo.periodicity == ProductPeriodicity.MONTHLY &&
        introductoryPricingPhase.cycleCount > 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isYearlySinglePayment() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.cycleCount == 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isMonthlyRecurringPayment() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.MONTHLY && introductoryPricingPhase.cycleCount > 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isMonthlySinglePaymentOfferRenewingToYearly() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.YEARLY &&
        introductoryPricingPhase.billingInfo.periodicity == ProductPeriodicity.MONTHLY &&
        introductoryPricingPhase.cycleCount == 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isMonthlySinglePayment() =
    basePricingPhase.billingInfo.periodicity == ProductPeriodicity.MONTHLY && introductoryPricingPhase.cycleCount == 1

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isFreeTrialForYearlyPlan() =
    introductoryPricingPhase.priceMicro == 0L &&
        basePricingPhase.billingInfo.periodicity != introductoryPricingPhase.billingInfo.periodicity

private fun ProductDetailsWrapper.IntroductoryOfferProduct.isMonthlyFreeTrial() =
    introductoryPricingPhase.priceMicro == 0L &&
        basePricingPhase.billingInfo.periodicity == introductoryPricingPhase.billingInfo.periodicity

fun OfferType.plansResId(): Int = when (this) {
    OfferType.ADVANCED -> R.string.plans_advanced_title
    OfferType.PREMIUM -> R.string.plans_premium_title
    OfferType.FAMILY -> R.string.plans_family_title
}
