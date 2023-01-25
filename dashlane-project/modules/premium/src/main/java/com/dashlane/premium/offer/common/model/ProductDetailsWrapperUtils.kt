package com.dashlane.premium.offer.common.model

import java.text.NumberFormat
import java.util.Locale

fun ProductDetailsWrapper.IntroductoryOfferProduct.computeDiscountPercentage(localeId: String): String {
    val basePlanTotalLength = basePricingPhase.billingInfo.length
    val basePlanPriceValue: Long = basePricingPhase.priceMicro / basePlanTotalLength

    val introTotalLength = introductoryPricingPhase.billingInfo.length
    val introPriceValue: Long = introductoryPricingPhase.priceMicro / introTotalLength

    val discount = 1 - introPriceValue.toFloat() / basePlanPriceValue.toFloat()
    return NumberFormat.getPercentInstance(
        Locale.Builder().setLanguage(localeId).build()
    ).apply { maximumFractionDigits = 0 }.format(discount)
}