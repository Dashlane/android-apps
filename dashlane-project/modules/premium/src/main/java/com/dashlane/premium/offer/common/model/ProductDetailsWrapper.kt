package com.dashlane.premium.offer.common.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import java.time.Period

sealed class ProductDetailsWrapper {
    abstract val originalProductDetails: ProductDetails
    abstract val productId: String
    abstract val basePlanOffer: Offer
    abstract val basePricingPhase: PricingPhase

    data class BasePlanProduct(
        override val originalProductDetails: ProductDetails,
        override val productId: String,
        private val offer: Offer
    ) : ProductDetailsWrapper() {
        override val basePlanOffer: Offer
            get() = offer

        override val basePricingPhase: PricingPhase
            get() = offer.pricingPhases.first()
    }

    data class IntroductoryOfferProduct(
        override val originalProductDetails: ProductDetails,
        override val productId: String,
        private val offers: List<Offer>
    ) : ProductDetailsWrapper() {
        override val basePlanOffer: Offer
            get() = offers.last()

        override val basePricingPhase: PricingPhase
            get() = basePlanOffer.pricingPhases.first()

        val introductoryPlanOffer: Offer
            get() = offers.first()

        val introductoryPricingPhase: PricingPhase
            get() = introductoryPlanOffer.pricingPhases.first()
    }

    data class Offer(
        val offerIdToken: String,
        val pricingPhases: List<PricingPhase>,
        val tags: List<String>
    ) {
        

        val idTag: String?
            get() = tags.firstOrNull {
                it.startsWith("id-")
            }
    }

    data class BillingInfo(
        val periodicity: ProductPeriodicity,
        val length: Int
    )

    data class PricingPhase(
        val priceMicro: Long,
        val priceCurrencyCode: String,
        val formattedPrice: String,
        val cycleCount: Int,
        val billingInfo: BillingInfo
    )

    companion object {
        fun fromProductDetailsOrNull(productDetails: ProductDetails): ProductDetailsWrapper? {
            if (productDetails.productType != BillingClient.ProductType.SUBS) {
                return null 
            }

            val offers = productDetails.subscriptionOfferDetails?.map { offer ->
                val pricingPhases = offer.pricingPhases.pricingPhaseList.map { pricingPhase ->
                    val billingPeriod = pricingPhase.billingPeriod.parseBillingPeriod() ?: return null
                    PricingPhase(
                        priceMicro = pricingPhase.priceAmountMicros,
                        priceCurrencyCode = pricingPhase.priceCurrencyCode,
                        formattedPrice = pricingPhase.formattedPrice,
                        billingInfo = billingPeriod,
                        cycleCount = pricingPhase.billingCycleCount
                    )
                }
                Offer(
                    offerIdToken = offer.offerToken,
                    pricingPhases = pricingPhases,
                    tags = offer.offerTags
                )
            } ?: return null

            return if (offers.size == 1) {
                BasePlanProduct(
                    originalProductDetails = productDetails,
                    productId = productDetails.productId,
                    offer = offers.first()
                )
            } else {
                IntroductoryOfferProduct(
                    originalProductDetails = productDetails,
                    productId = productDetails.productId,
                    offers = offers
                )
            }
        }

        private fun String.parseBillingPeriod(): BillingInfo? {
            val period = Period.parse(this)

            
            
            
            return when (this.last()) {
                'M' -> {
                    BillingInfo(
                        length = period.months,
                        periodicity = ProductPeriodicity.MONTHLY
                    )
                }
                'Y' -> {
                    BillingInfo(
                        length = period.years,
                        periodicity = ProductPeriodicity.YEARLY
                    )
                }
                else -> null
            }
        }
    }
}