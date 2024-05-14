package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

internal object YearlySavingCalculator {

    private const val MICRO = 1_000_000.0f

    operator fun invoke(offer: FormattedStoreOffer): Float? {
        val monthlyRate = offer.monthly?.productDetails?.basePricingPhase?.priceMicro
        val yearlyRate = offer.yearly?.productDetails?.basePricingPhase?.priceMicro

        if (monthlyRate == null || yearlyRate == null) {
            return null
        }
        val savingMicros = (12 * monthlyRate - yearlyRate)
        val saving = savingMicros / MICRO
        return if (saving > 0) saving else null
    }

    fun getFormatter(formattedOffers: List<FormattedStoreOffer>): NumberFormat? {
        val currencyCode = getFirstCurrencyCode(formattedOffers) ?: return null
        return getCurrencyFormatter(currencyCode)
    }

    private fun getFirstCurrencyCode(formattedOffers: List<FormattedStoreOffer>) =
        formattedOffers.asSequence()
            .map {
                val monthlyCode = it.monthly?.productDetails?.basePricingPhase?.priceCurrencyCode
                val yearlyCode = it.yearly?.productDetails?.basePricingPhase?.priceCurrencyCode
                monthlyCode ?: yearlyCode
            }
            .firstOrNull { it != null }

    private fun getCurrencyFormatter(currencyCode: String): NumberFormat =
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
            roundingMode = RoundingMode.HALF_DOWN
        }
}